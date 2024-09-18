package net.sdm.sdmshoprework.client.screen.modern.buyer;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.sdm.sdmeconomy.api.CurrencyHelper;
import net.sdm.sdmshoprework.SDMShopRework;
import net.sdm.sdmshoprework.client.screen.basic.buyer.AbstractBuyerBuyButton;
import net.sdm.sdmshoprework.client.screen.basic.buyer.AbstractBuyerCancelButton;
import net.sdm.sdmshoprework.client.screen.basic.buyer.AbstractBuyerScreen;
import net.sdm.sdmshoprework.client.screen.basic.widget.AbstractShopEntryButton;
import net.sdm.sdmshoprework.client.screen.modern.ModernShopScreen;
import net.sdm.sdmshoprework.common.shop.type.ShopItemEntryType;
import net.sixik.sdmuilibrary.client.utils.GLHelper;
import net.sixik.sdmuilibrary.client.utils.math.Vector2;
import net.sixik.sdmuilibrary.client.utils.misc.RGBA;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ModernBuyerScreen extends AbstractBuyerScreen {

    public TextBox textBox;

    int sizeIcon;
    int howMane;
    public Component entryType;

    @Override
    public boolean drawDefaultBackground(GuiGraphics graphics) {
        return false;
    }

    public ModernShopScreen screen;
    public AbstractShopEntryButton entryButton;

    public ModernBuyerScreen(ModernShopScreen screen, AbstractShopEntryButton shopEntry) {
        this.screen = screen;
        this.entryButton = shopEntry;
        this.shopEntry = shopEntry.entry;
    }

    @Override
    public void addWidgets() {
        add(this.textBox = new TextBox(this) {
            @Override
            public boolean isValid(String txt) {
                return parse((Consumer)null, txt, 1, howMane);
            }

            @Override
            public void onTextChanged() {
                if(!getText().isEmpty())
                    count = Integer.parseInt(getText());
            }

            @Override
            public void drawTextBox(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
                RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics,x,y,w,h, 4);
            }
        });
        add(this.cancelButton = new CancelButton(this));
        add(this.buyButton = new BuyButton(this));

        setProperty();
    }

    @Override
    public void alignWidgets() {
        setProperty();
    }

    @Override
    public void setProperty() {
        int bsize = this.width / 2 - 10;
        this.sizeIcon = width >= 16 ? 16 : 8;

        this.cancelButton.setPosAndSize(8, this.height - 24, bsize, 16);
        this.buyButton.setPosAndSize(this.width - bsize - 8, this.height - 24, bsize, 16);

        this.textBox.setText(count > 0 ? String.valueOf(count) : "");
        this.textBox.ghostText = shopEntry.isSell ? Component.translatable("sdm.shop.modern.ui.buyer.entry.input.ghost.sell").getString() : Component.translatable("sdm.shop.modern.ui.buyer.entry.input.ghost.buy").getString();
        this.textBox.setPos(5, 5 + sizeIcon * 2 + 2 + (Minecraft.getInstance().font.lineHeight + 1 + 2) * 2);
        this.textBox.setSize(this.width - 10, Minecraft.getInstance().font.lineHeight + 1);


        updateButtons();
    }

    public void updateButtons(){
        howMane = shopEntry.getEntryType().howMany(Minecraft.getInstance().player, shopEntry.isSell, shopEntry);
        entryType = shopEntry.isSell ? Component.translatable("sdm.shop.modern.ui.buyer.entry.sell")
                : Component.translatable("sdm.shop.modern.ui.buyer.entry.buy");
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, x, y, w, h, 10);

        Vector2 pos = new Vector2(x + 5, y + 5);

        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, pos.x,pos.y, sizeIcon * 2, sizeIcon * 2, 8);
        entryButton.entry.getEntryType().getIcon().draw(graphics,pos.x + sizeIcon / 2,pos.y + sizeIcon / 2,sizeIcon,sizeIcon);

        pos.setX(pos.x + sizeIcon * 2 + 2);

        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, pos.x,pos.y, this.width - 10 - 2 - sizeIcon * 2, Minecraft.getInstance().font.lineHeight + 1, 4);
        if(entryButton.entry.getEntryType() instanceof ShopItemEntryType entryType) {
            String d = entryType.itemStack.getDisplayName().getString();
            d = d.replace("[", "").replace("]", "");
            theme.drawString(graphics, d, pos.x + 2, pos.y + 1, Color4I.WHITE, 2);
        }


        String textMoney = SDMShopRework.moneyString(entryButton.entry.entryPrice);

        pos.setY(pos.y + sizeIcon);
        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, pos.x,pos.y, this.width - 10 - 2 - sizeIcon * 2, Minecraft.getInstance().font.lineHeight + 1, 4);
        theme.drawString(graphics, textMoney, pos.x + 2, pos.y + 1, Color4I.WHITE, 2);

        pos.setPosition(x + 5, y + 5 + sizeIcon * 2 + 2);
        Vector2 size = new Vector2(this.width - 10, this.height - (5 + sizeIcon * 2 + 2 + 24 + 2));

        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, pos.x,pos.y, size.x / 2 - 2, Minecraft.getInstance().font.lineHeight + 1, 4);

        GLHelper.pushScissor(graphics, pos.x,pos.y, size.x / 2 - 2, Minecraft.getInstance().font.lineHeight + 1);
        theme.drawString(graphics, Component.translatable("sdm.shop.modern.ui.buyer.player_money"), pos.x + 2, pos.y + 1, Color4I.WHITE, 2);
        GLHelper.popScissor(graphics);

        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, pos.x + size.x / 2,pos.y, size.x / 2, Minecraft.getInstance().font.lineHeight + 1, 4);

        textMoney = SDMShopRework.moneyString(CurrencyHelper.Basic.getMoney(Minecraft.getInstance().player));

        GLHelper.pushScissor(graphics, pos.x + size.x / 2, pos.y, size.x / 2 - 2, Minecraft.getInstance().font.lineHeight + 1);
        theme.drawString(graphics, textMoney, pos.x + size.x / 2 + 2, pos.y + 1, Color4I.WHITE, 2);
        GLHelper.popScissor(graphics);

        pos.setPosition(pos.x, pos.y + Minecraft.getInstance().font.lineHeight + 1 + 2);
        drawNewLabel(graphics, theme, pos, size, entryType.getString(), String.valueOf(howMane));

        pos.setPosition(pos.x, pos.y + (Minecraft.getInstance().font.lineHeight + 1 + 2) * 2);
        textMoney = shopEntry.isSell ? Component.translatable("sdm.shop.modern.ui.buyer.entry.output.sell").getString() : Component.translatable("sdm.shop.modern.ui.buyer.entry.output.buy").getString();

        drawNewLabel(graphics, theme, pos, size, textMoney, String.valueOf(shopEntry.entryPrice * count));

//        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, pos.x,pos.y, this.width - 10, this.height - (5 + sizeIcon * 2 + 2 + 24 + 2), 4);
    }

    public void drawNewLabel(GuiGraphics graphics, Theme theme, Vector2 pos, Vector2 size, String left, String right) {
        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, pos.x,pos.y, size.x / 2 - 2, Minecraft.getInstance().font.lineHeight + 1, 4);

        GLHelper.pushScissor(graphics, pos.x,pos.y, size.x / 2 - 2, Minecraft.getInstance().font.lineHeight + 1);
        theme.drawString(graphics, left, pos.x + 2, pos.y + 1, Color4I.WHITE, 2);
        GLHelper.popScissor(graphics);

        RGBA.create(0, 0, 0, 255 / 2).drawRoundFill(graphics, pos.x + size.x / 2,pos.y, size.x / 2, Minecraft.getInstance().font.lineHeight + 1, 4);

        GLHelper.pushScissor(graphics, pos.x + size.x / 2, pos.y, size.x / 2 - 2, Minecraft.getInstance().font.lineHeight + 1);
        theme.drawString(graphics, right, pos.x + size.x / 2 + 2, pos.y + 1, Color4I.WHITE, 2);
        GLHelper.popScissor(graphics);
    }


    public boolean parse(@Nullable Consumer<Integer> callback, String string, int min, int max) {
        try {
            int v = Long.decode(string).intValue();
            if (v >= (Integer)min && v <= (Integer)max) {
                if (callback != null) {
                    callback.accept(v);
                }

                return true;
            }
        } catch (Exception var4) {
        }

        return false;
    }

    public static class CancelButton extends AbstractBuyerCancelButton {
        public CancelButton(ModernBuyerScreen modernBuyerScreen) {
            super(modernBuyerScreen);
        }

        @Override
        public boolean renderTitleInCenter() {
            return true;
        }


        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            RGBA.create(0,0,0,255 / 2).drawRoundFill(graphics, x,y,w,h, 6);
        }
    }

    public static class BuyButton extends AbstractBuyerBuyButton {
        public BuyButton(ModernBuyerScreen modernBuyerScreen) {
            super(modernBuyerScreen);
        }

        @Override
        public boolean renderTitleInCenter() {
            return true;
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            RGBA.create(0,0,0,255 / 2).drawRoundFill(graphics, x,y,w,h, 6);
        }
    }
}
