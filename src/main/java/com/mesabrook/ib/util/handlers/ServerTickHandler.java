package com.mesabrook.ib.util.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import com.mesabrook.ib.Main;
import com.mesabrook.ib.apimodels.company.LocationEmployee;
import com.mesabrook.ib.apimodels.company.LocationItem;
import com.mesabrook.ib.blocks.te.TileEntityRegister;
import com.mesabrook.ib.capability.employee.CapabilityEmployee;
import com.mesabrook.ib.capability.employee.IEmployeeCapability;
import com.mesabrook.ib.net.sco.StoreModeGuiResponse;
import com.mesabrook.ib.util.apiaccess.DataAccess;
import com.mesabrook.ib.util.apiaccess.DataAccess.API;
import com.mesabrook.ib.util.apiaccess.DataAccess.GenericErrorResponse;
import com.mesabrook.ib.util.apiaccess.DataRequestQueue;
import com.mesabrook.ib.util.apiaccess.DataRequestTask;
import com.mesabrook.ib.util.apiaccess.DataRequestTaskStatus;
import com.mesabrook.ib.util.apiaccess.GetData;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.items.CapabilityItemHandler;

@EventBusSubscriber
public class ServerTickHandler {

	// Used to stagger checks. Methods using this should try to avoid too much happening on any given tick. Values 0-20
	private static byte checkerCounter = 0;
	@SubscribeEvent
	public static void serverTick(ServerTickEvent e)
	{
		if (e.phase != Phase.END)
		{
			return;
		}
		
		checkerCounter++;
		if (checkerCounter > 20)
		{
			checkerCounter = 0;
		}
		checkStoreModeRequests();
		updateEmployeeStoreModes();
		handlePriceLookups();
	}
	
	public static HashMap<UUID, DataRequestTask> storeModeRequestsByUser = new HashMap<UUID, DataRequestTask>();
	private static void checkStoreModeRequests()
	{
		if (checkerCounter != 0 || storeModeRequestsByUser.size() == 0)
		{
			return;
		}
		
		HashSet<UUID> uuidsToRemove = new HashSet<>();
		for(Map.Entry<UUID, DataRequestTask> entry : storeModeRequestsByUser.entrySet())
		{
			DataRequestTask task = entry.getValue();
			if (task.getStatus() == DataRequestTaskStatus.Complete)
			{
				DataAccess access = task.getTask();
				if (access.getRequestSuccessful())
				{
					LocationEmployee[] locationEmployees = access.getResult(LocationEmployee[].class);
					EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(entry.getKey());
					
					StoreModeGuiResponse response = new StoreModeGuiResponse();
					response.locationEmployees = locationEmployees;
					PacketHandler.INSTANCE.sendTo(response, player);
				}
				
				uuidsToRemove.add(entry.getKey());
			}
		}
		
		for(UUID idToRemove : uuidsToRemove)
		{
			storeModeRequestsByUser.remove(idToRemove);
		}
	}
	
	private static HashMap<UUID, DataRequestTask> employeeStoreModeRequests = new HashMap<>();
	private static int updateEmployeeStoreModesChecker = 0;
	private static void updateEmployeeStoreModes()
	{
		if (++updateEmployeeStoreModesChecker < 100)
		{
			return;
		}
		
		updateEmployeeStoreModesChecker = 0;
		
		for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers())
		{
			if (employeeStoreModeRequests.containsKey(player.getUniqueID()))
			{
				handleEmployeeStoreModeRequest(player);
			}
			else
			{
				IEmployeeCapability employeeCap = player.getCapability(CapabilityEmployee.EMPLOYEE_CAPABILITY, null);
				if (employeeCap.getLocationID() == 0)
				{
					employeeCap.serverToClientSync();
					return;
				}
				
				enqueueStoreModeUpdateNow(player);
			}
			
		}
	}
	
	private static void handleEmployeeStoreModeRequest(EntityPlayerMP player)
	{
		DataRequestTask task = employeeStoreModeRequests.get(player.getUniqueID());
		if (task.getStatus() != DataRequestTaskStatus.Complete)
		{
			return;
		}
		employeeStoreModeRequests.remove(player.getUniqueID());
		
		if (!task.getTask().getRequestSuccessful() && task.getTask().getResponseCode() != 404)
		{
			GenericErrorResponse errorResponse = task.getTask().getResult(GenericErrorResponse.class);
			String logError = "An unknown error occurred while handling employee store mode request";
			if (errorResponse != null)
			{
				logError = errorResponse.message;
			}
			
			Main.logger.error(logError);
			return;
		}
		
		LocationEmployee locationEmployee = new LocationEmployee();
		locationEmployee.LocationID = 0;
		if (task.getTask().getResponseCode() != 404)
		{				
			locationEmployee = task.getTask().getResult(LocationEmployee.class);
		}
		IEmployeeCapability employeeCap = player.getCapability(CapabilityEmployee.EMPLOYEE_CAPABILITY, null);
		employeeCap.setLocationEmployee(locationEmployee);
		employeeCap.serverToClientSync();
	}
	
	public static void enqueueStoreModeUpdateNow(EntityPlayerMP player)
	{
		IEmployeeCapability employeeCap = player.getCapability(CapabilityEmployee.EMPLOYEE_CAPABILITY, null);
		
		GetData get = new GetData(API.Company, "LocationEmployeeIBAccess/GetLocationForPlayerLocation", LocationEmployee.class);
		get.addQueryString("player", player.getName());
		get.addQueryString("locationID", Long.toString(employeeCap.getLocationID()));
		
		DataRequestTask task = new DataRequestTask(get);
		employeeStoreModeRequests.put(player.getUniqueID(), task);
		DataRequestQueue.INSTANCE.addTask(task);
	}
	
	public static ArrayList<DataRequestTask> priceLookupTasks = new ArrayList<>();
	private static void handlePriceLookups()
	{
		ArrayList<DataRequestTask> tasksToRemove = new ArrayList<>();
		for(DataRequestTask task : priceLookupTasks)
		{
			if (task.getStatus() == DataRequestTaskStatus.Complete)
			{				
				DataAccess access = task.getTask();
				if (access.getRequestSuccessful())
				{
					BlockPos registerPos = BlockPos.fromLong((long)task.getData().get("pos"));
					int slot = (int)task.getData().get("slot");
					int dimensionID = (int)task.getData().get("dimId");
					
					World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimensionID);
					if (world != null)
					{
						TileEntity te = world.getTileEntity(registerPos);
						if (te instanceof TileEntityRegister)
						{
							TileEntityRegister register = (TileEntityRegister)te;
							TileEntityRegister.RegisterItemHandler itemHandler = (TileEntityRegister.RegisterItemHandler)register.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
							LocationItem locationItem = access.getResult(LocationItem.class);
							itemHandler.setPrice(slot, locationItem.BasePrice);
						}
					}
				}
				tasksToRemove.add(task);
			}
		}
		
		for(DataRequestTask taskToRemove : tasksToRemove)
		{
			priceLookupTasks.remove(taskToRemove);
		}
	}
}
