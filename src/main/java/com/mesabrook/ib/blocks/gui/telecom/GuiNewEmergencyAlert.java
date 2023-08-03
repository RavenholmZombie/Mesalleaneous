package com.mesabrook.ib.blocks.gui.telecom;

import akka.actor.ActorPathExtractor$;
import com.mesabrook.ib.init.SoundInit;
import com.mesabrook.ib.net.ClientSoundPacket;
import com.mesabrook.ib.net.telecom.*;
import com.mesabrook.ib.util.Reference;
import com.mesabrook.ib.util.handlers.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class GuiNewEmergencyAlert extends GuiPhoneBase
{
    String[] text = new String[] { "THIS IS A TEST OF THE NATIONAL WIRELESS EMERGENCY ALERT SYSTEM. HAD THIS BEEN AN ACTUAL EMERGENCY, OFFICIAL INFORMATION WOULD BE PROVIDED TO YOU THROUGH THIS SYSTEM. THIS IS ONLY A TEST." };

    MinedroidButton dismiss;
    MinedroidButton nextPage;
    MinedroidButton backPage;
    ImageButton icon;

    private final String header = new TextComponentString(TextFormatting.BOLD + "EMERGENCY ALERT").getFormattedText();
    private int maxLinesShown = 0;
    private int boxWidth = 0;
    private int boxHeight = 0;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentLabel;
    private String currentText;
    public static HashMap<Integer, String> labelsByNumber = new HashMap<>();
    public static HashMap<Integer, String> textByNumber = new HashMap<>();

    public GuiNewEmergencyAlert(ItemStack phoneStack, EnumHand hand)
    {
        super(phoneStack, hand);

        currentLabel = labelsByNumber.get(phoneStackData.getPhoneNumber());
        currentText = textByNumber.get(phoneStackData.getPhoneNumber());

        labelsByNumber.remove(phoneStackData.getPhoneNumber());
        textByNumber.remove(phoneStackData.getPhoneNumber());

        currentLabel = currentLabel != null ? currentLabel : "";
        currentText = currentText != null ? currentText : "";
    }

    @Override
    protected String getInnerTextureFileName()
    {
        return "system/app_splash_red.png";
    }

    @Override
    protected boolean renderTopBar() {
        return false;
    }

    @Override
    protected boolean renderControlBar() {
        return false;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        boxWidth = width - 20;
        maxLinesShown = (INNER_TEX_HEIGHT - 120) / fontRenderer.FONT_HEIGHT;
        boxHeight = 22 + fontRenderer.FONT_HEIGHT * maxLinesShown;

        int lowerControlsY = INNER_Y + INNER_TEX_HEIGHT - INNER_TEX_Y_OFFSET - 50;
        backPage = new MinedroidButton(1, INNER_X + 15, lowerControlsY - 3, 35, "<<", 0xFFFFFF);
        nextPage = new MinedroidButton(2, INNER_X + 115, lowerControlsY - 3, 35, ">>", 0xFFFFFF);
        dismiss = new MinedroidButton(3, INNER_X + 62, lowerControlsY + 15, 40, "Dismiss", 0xFFFFFF);

        buttonList.add(backPage);
        buttonList.add(nextPage);
        buttonList.add(dismiss);
    }

    @Override
    protected void doDraw(int mouseX, int mouseY, float partialticks)
    {
        super.doDraw(mouseX, mouseY, partialticks);
        drawCenteredString(fontRenderer, new TextComponentString(TextFormatting.BOLD + "EMERGENCY ALERT").getFormattedText(), INNER_X + 80, INNER_Y + 8, 0xFFFFFF);
        drawCenteredString(fontRenderer, new TextComponentString(TextFormatting.ITALIC + currentLabel.toUpperCase(Locale.ROOT)).getFormattedText(), INNER_X + 80, INNER_Y + 22, 0xFFFFFF);

        drawRect(INNER_X + 3, INNER_Y + 40, INNER_X + 159, INNER_Y + 150, 0xFFAAAAAA);
        drawRect(INNER_X + 5, INNER_Y + 42, INNER_X + 157, INNER_Y + 148, 0xFF000000);
        for(int i = (currentPage - 1) * maxLinesShown; i < maxLinesShown * currentPage; i++)
        {
            if (i >= text.length)
            {
                break;
            }

            String line = text[i];
            fontRenderer.drawString(line, INNER_X + 5, INNER_Y + 42 + fontRenderer.FONT_HEIGHT * (i - (currentPage - 1) * maxLinesShown), 0xFFFFFF);
        }

        String[] untruncatedLines = currentText.split("\\\\n");
        ArrayList<String> lines = new ArrayList<>();
        for(String untruncatedLine : untruncatedLines)
        {
            for(String truncatedLine : fontRenderer.listFormattedStringToWidth(untruncatedLine, INNER_TEX_WIDTH - 10))
            {
                lines.add(truncatedLine);
            }
        }

        totalPages = lines.size() / maxLinesShown + 1;
        this.text = lines.toArray(new String[0]);

        int lowerControlsY = INNER_Y + INNER_TEX_HEIGHT - INNER_TEX_Y_OFFSET - 50;
        String pageCountLabel = String.format("Page %s/%s", currentPage, totalPages);
        drawCenteredString(fontRenderer, pageCountLabel, width / 2, lowerControlsY, 0xFFFFFF);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        if (button == nextPage && currentPage != totalPages)
        {
            currentPage ++;
        }

        if (button == backPage && currentPage > 1)
        {
            currentPage--;
        }

        if(button == dismiss)
        {
            if(isPhoneUnlocked)
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiHome(phoneStack, hand));
            }
            else
            {
                Minecraft.getMinecraft().displayGuiScreen(new GuiLockScreen(phoneStack, hand));
            }
        }
    }
}
