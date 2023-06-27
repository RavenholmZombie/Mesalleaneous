package com.mesabrook.ib.blocks.gui.telecom;

import com.mesabrook.ib.net.ClientSoundPacket;
import com.mesabrook.ib.util.Reference;
import com.mesabrook.ib.util.handlers.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;


public class GuiMSACBootScreen extends GuiPhoneBase
{
    private int timerToNextScreen = 0;
    private int fadeAnimationTimer = 0;
    private String currentTexture;
    private String bootingText;

    public GuiMSACBootScreen(ItemStack phoneStack, EnumHand hand)
    {
        super(phoneStack, hand);
        fadeAnimationTimer++;
    }

    @Override
    protected String getInnerTextureFileName()
    {
        if(fadeAnimationTimer == 0)
        {
            currentTexture = "system/boot_screen_msac_0.png";
        }
        if(fadeAnimationTimer == 50)
        {
            currentTexture = "system/boot_screen_msac_0.png";
        }
        if(fadeAnimationTimer == 100)
        {
            currentTexture = "system/boot_screen_msac_1.png";
            bootingText = "Starting Minedroid";
        }
        if(fadeAnimationTimer == 150)
        {
            currentTexture = "system/boot_screen_msac_2.png";
        }
        if(fadeAnimationTimer == 200)
        {
            currentTexture = "system/boot_screen_msac_3.png";
        }
        if(fadeAnimationTimer == 250)
        {
            currentTexture = "system/boot_screen_msac_4.png";
        }
        if(fadeAnimationTimer == 300)
        {
            currentTexture = "system/boot_screen_msac_5.png";
        }
        if(fadeAnimationTimer == 400)
        {
            currentTexture = "system/boot_screen_msac_6.png";
        }
        if(fadeAnimationTimer == 500)
        {
            currentTexture = "system/boot_screen_msac_1.png";
        }

        if(fadeAnimationTimer > 550)
        {
            fadeAnimationTimer = 50;
        }

        return currentTexture;
    }

    @Override
    protected boolean renderControlBar() {
        return false;
    }

    @Override
    protected boolean renderTopBar() {
        return false;
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    @Override
    protected void doDraw(int mouseX, int mouseY, float partialticks)
    {
        super.doDraw(mouseX, mouseY, partialticks);
        timerToNextScreen++;
        fadeAnimationTimer++;

        drawCenteredString(fontRenderer, bootingText, INNER_X + 80, INNER_Y + 180, 0xFFFFFF);

        if(timerToNextScreen >= 1500)
        {
            finishBoot();
        }
    }

    private void finishBoot()
    {
        if(phoneStackData.getNeedToDoOOBE())
        {
            GuiFirstPhoneBoot boot = new GuiFirstPhoneBoot(phoneStack, hand);
            Minecraft.getMinecraft().displayGuiScreen(boot);
        }
        else
        {
            GuiBootScreen boot = new GuiBootScreen(phoneStack, hand);
            Minecraft.getMinecraft().displayGuiScreen(boot);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        timerToNextScreen = 0;
        fadeAnimationTimer = 0;
    }
}
