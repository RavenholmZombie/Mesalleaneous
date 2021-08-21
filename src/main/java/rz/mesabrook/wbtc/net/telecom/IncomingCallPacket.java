package rz.mesabrook.wbtc.net.telecom;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rz.mesabrook.wbtc.util.handlers.ClientSideHandlers.TelecomClientHandlers;

public class IncomingCallPacket implements IMessage {

	public String fromNumber;
	public String toNumber;
	@Override
	public void fromBytes(ByteBuf buf) {
		fromNumber = ByteBufUtils.readUTF8String(buf);
		toNumber = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, fromNumber);
		ByteBufUtils.writeUTF8String(buf, toNumber);
	}
	
	public static class Handler implements IMessageHandler<IncomingCallPacket, IMessage>
	{

		@Override
		public IMessage onMessage(IncomingCallPacket message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(IncomingCallPacket message, MessageContext ctx)
		{
			TelecomClientHandlers.onIncomingCall(message.fromNumber, message.toNumber);
		}
	}
}
