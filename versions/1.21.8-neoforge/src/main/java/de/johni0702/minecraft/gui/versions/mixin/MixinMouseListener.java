package de.johni0702.minecraft.gui.versions.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.johni0702.minecraft.gui.function.Click;
import de.johni0702.minecraft.gui.versions.callbacks.MouseCallback;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MixinMouseListener {
    @Accessor
    abstract int getActiveButton();

    @Inject(
            method = "onMouseButton",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"),
            cancellable = true
    )
    private void mouseDown(
            long window, int button, int action, int mods,
            CallbackInfo ci,
            @Local(ordinal = 0) double x,
            @Local(ordinal = 1) double y
    ) {
        if (MouseCallback.EVENT.invoker().mouseDown(new Click(x, y, button))) {
            ci.cancel();
        }
    }

    @Inject(
            method = "onMouseButton",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(DDI)Z"),
            cancellable = true
    )
    private void mouseUp(
            long window, int button, int action, int mods,
            CallbackInfo ci,
            @Local(ordinal = 0) double x,
            @Local(ordinal = 1) double y
    ) {
        if (MouseCallback.EVENT.invoker().mouseUp(new Click(x, y, button))) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseDragged(DDIDD)Z"),
            cancellable = true
    )
    private void mouseDrag(
            CallbackInfo ci,
            @Local(ordinal = 2) double x,
            @Local(ordinal = 3) double y,
            @Local(ordinal = 4) double dx,
            @Local(ordinal = 5) double dy
    ) {
        if (MouseCallback.EVENT.invoker().mouseDrag(new Click(x, y, getActiveButton()), dx, dy)) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "onMouseScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDDD)Z")
    )
    private boolean mouseScroll(
            Screen screen, double x, double y, double horizontal, double vertical,
            Operation<Boolean> original
    ) {
        if (MouseCallback.EVENT.invoker().mouseScroll(x, y, horizontal, vertical)) {
            return true;
        }
        return original.call(screen, x, y, horizontal, vertical);
    }
}
