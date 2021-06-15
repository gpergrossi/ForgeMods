package com.gpergrossi.aerogen.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAerogenWorldSettingsScreen extends GuiScreen {

	private final GuiCreateWorld parent;
    protected String settingsJSON;
    
    protected String title = "Customize World Settings";
    protected String message = "Not yet implemented.";
	
    public GuiAerogenWorldSettingsScreen(GuiCreateWorld parent, String settingsJSON) {
        this.parent = parent;
        this.loadValues(settingsJSON);
    }

	private void loadValues(String settingsJSON) {
		System.out.println("Settings: "+settingsJSON);
		this.settingsJSON = settingsJSON;
		
		// TODO remove this
		this.settingsJSON = "{regionGridSize:256.0}";
	}
 
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
	public void initGui() {
        this.title = I18n.format("options.customizeTitle");
        this.buttonList.clear();
        
        this.addButton(new GuiButton(300, this.width / 2 + 98, this.height - 27, 90, 20, I18n.format("gui.done")));
    }
    
    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
	protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        
        switch (button.id) {
            case 300:
                this.parent.chunkProviderSettingsJson = this.settingsJSON;
                this.mc.displayGuiScreen(this.parent);
                break;
        }
    }
    
    /**
     * Draws the screen and all the components in it.
     */
    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 2, 16777215);
        this.drawCenteredString(this.fontRenderer, this.message, this.width / 2, 12, 16777215);
        
        drawGradientRect(12, 22, this.width-12, this.height-27-12, 0xFF000000, 0xFFFFFFFF);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
	
}
