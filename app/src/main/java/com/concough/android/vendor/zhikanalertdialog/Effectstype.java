package com.concough.android.vendor.zhikanalertdialog;

import com.concough.android.vendor.zhikanalertdialog.effects.BaseEffects;
import com.concough.android.vendor.zhikanalertdialog.effects.FadeIn;
import com.concough.android.vendor.zhikanalertdialog.effects.Fall;
import com.concough.android.vendor.zhikanalertdialog.effects.FlipH;
import com.concough.android.vendor.zhikanalertdialog.effects.FlipV;
import com.concough.android.vendor.zhikanalertdialog.effects.NewsPaper;
import com.concough.android.vendor.zhikanalertdialog.effects.RotateBottom;
import com.concough.android.vendor.zhikanalertdialog.effects.RotateLeft;
import com.concough.android.vendor.zhikanalertdialog.effects.Shake;
import com.concough.android.vendor.zhikanalertdialog.effects.SideFall;
import com.concough.android.vendor.zhikanalertdialog.effects.SlideBottom;
import com.concough.android.vendor.zhikanalertdialog.effects.SlideLeft;
import com.concough.android.vendor.zhikanalertdialog.effects.SlideRight;
import com.concough.android.vendor.zhikanalertdialog.effects.SlideTop;
import com.concough.android.vendor.zhikanalertdialog.effects.Slit;

/**
 * Created by lee on 2014/7/30.
 */
public enum  Effectstype {

    Fadein(FadeIn.class),
    Slideleft(SlideLeft.class),
    Slidetop(SlideTop.class),
    SlideBottom(SlideBottom.class),
    Slideright(SlideRight.class),
    Fall(Fall.class),
    Newspager(NewsPaper.class),
    Fliph(FlipH.class),
    Flipv(FlipV.class),
    RotateBottom(RotateBottom.class),
    RotateLeft(RotateLeft.class),
    Slit(Slit.class),
    Shake(Shake.class),
    Sidefill(SideFall.class);
    private Class<? extends BaseEffects> effectsClazz;

    private Effectstype(Class<? extends BaseEffects> mclass) {
        effectsClazz = mclass;
    }

    public BaseEffects getAnimator() {
        BaseEffects bEffects=null;
	try {
		bEffects = effectsClazz.newInstance();
	} catch (ClassCastException e) {
		throw new Error("Can not init animatorClazz instance");
	} catch (InstantiationException e) {
		throw new Error("Can not init animatorClazz instance");
	} catch (IllegalAccessException e) {
		throw new Error("Can not init animatorClazz instance");
	}
	return bEffects;
    }
}
