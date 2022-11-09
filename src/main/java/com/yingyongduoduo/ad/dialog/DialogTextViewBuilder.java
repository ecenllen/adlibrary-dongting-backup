package com.yingyongduoduo.ad.dialog;

import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.yingyongduoduo.ad.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Sardonyxyu on 2018/3/9.
 */
public class DialogTextViewBuilder {
    private String title;
    private String content;
    private int titleSize;
    private int contentSize;
    private int contentColor;
    private String oneButton;
    private String twoButton;
    private TextView tvPrimary;
    private TextView tvSecondary;
    private DialogOnClickListener listener;

    public Context ctx;
    private Builder builder;

    public DialogTextViewBuilder(Builder builder) {
        this.ctx = builder.ctx;
        this.title = builder.title;
        this.content = builder.content;
        this.titleSize = builder.titleSize;
        this.contentSize = builder.contentSize;
        this.contentColor = builder.contentColor;
        this.oneButton = builder.oneButton;
        this.twoButton = builder.twoButton;
        this.tvPrimary = builder.tvPrimary;
        this.tvSecondary = builder.tvSecondary;
        this.listener = builder.listener;
        this.builder = builder.builder;
    }

    /**
     * 对话框关闭
     */
    public Builder getDialogBuilder() {
        return builder;
    }

    /**
     * 对话框关闭
     */
    public void dismissDialog() {
        if (builder != null) {
            builder.dismissDialog();
        }
    }

    /**
     * 获取确定按钮
     * @return
     */
    public TextView getPrimaryButton() {
        return tvPrimary;
    }

    /**
     * 获取取消按钮
     * @return
     */
    public TextView getSecondaryButton() {
        return tvSecondary;
    }

    public static class Builder extends Dialog implements OnClickListener {
        private final Context ctx;
        private final String title;
        private final String content;
        private int titleSize;
        private int contentSize;
        private int contentColor;
        private final String oneButton;
        private String twoButton;
        private boolean isClose = true;
        private boolean isSystemType = false;
        private DialogOnClickListener listener;

        private TextView tvTitle;
        private TextView tvContent;
        private TextView tvPrimary;
        private TextView tvSecondary;
        private Builder builder;

        /** 创建dialog，设置view布局到dialog
         * @param ctx
         * @param title
         * @param content
         * @param oneButton
         */
        public Builder(Context ctx, String title, String content, String oneButton) {
            super(ctx, R.style.ad_prefix_dialog);
            this.ctx = ctx;
            this.title = title;
            this.content = content;
            this.oneButton = oneButton;
            //用原本的布局
            setView(-100);
        }

        /** 创建dialog，设置view布局到dialog
         * @param ctx
         * @param title
         * @param content
         * @param oneButton
         * @param view 自定义布局，必须包含原本的控件id
         */
        public Builder(Context ctx, String title, String content, String oneButton, int view) {
            super(ctx, R.style.ad_prefix_dialog);
            this.ctx = ctx;
            this.title = title;
            this.content = content;
            this.oneButton = oneButton;
            setView(view);
        }

        /**
         * 监听
         * @param listener
         * @return
         */
        public Builder listener(DialogOnClickListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * 设置button按钮点击后不关闭Dialog
         * @return
         */
        public Builder notClose() {
            this.isClose = false;
            return this;
        }

        public Builder titleSize(int titleSize) {
            this.titleSize = titleSize;
            tvTitle.setTextSize(titleSize);
            return this;
        }

        public Builder contentSize(int contentSize) {
            this.contentSize = contentSize;
            tvContent.setTextSize(contentSize);
            return this;
        }

        public Builder contentColor(int contentColor) {
            this.contentColor = contentColor;
            tvContent.setTextColor(contentColor);
            return this;
        }

        public Builder isGravity(boolean isGravity) {
            if (!isGravity) {
                tvContent.setGravity(Gravity.CENTER_VERTICAL);
            }
            return this;
        }

        public Builder twoButton(String twoButton) {
            this.twoButton = twoButton;
            return this;
        }

        public Builder titleClick(View.OnClickListener onClickListener) {
            tvTitle.setOnClickListener(onClickListener);
            return this;
        }

        public Builder contentClick(View.OnClickListener onClickListener) {
            tvContent.setOnClickListener(onClickListener);
            return this;
        }

        public Builder setContentTextPartColor(String color, int start, int end) {
            SpannableString spannableString = new SpannableString(content);
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvContent.setText(spannableString);
            return this;
        }

        public Builder setContentGravity(int gravity) {
            tvContent.setGravity(gravity);
            return this;
        }

        /**
         * 返回键起不作用
         *
         * @return
         */
        public Builder isCancelable() {
            setCancelable(false);
            return this;
        }

        /**
         * 设置为系统类型的Dialog(可以在桌面显示)
         *
         * @return
         */
        public Builder isSystemType() {
            isSystemType = true;
            return this;
        }

        /**
         * @param isCanceledOnTouchOutside 点击对话框外是否关闭对话框
         * @return
         */
        public DialogTextViewBuilder build(boolean isCanceledOnTouchOutside) {
            setButton();
            setCanceledOnTouchOutside(isCanceledOnTouchOutside);
            if (isSystemType) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断是否拥有悬浮权限（6.0以后的）
                    if(!Settings.canDrawOverlays(ctx)) {
                        Toast.makeText(ctx, "请打开"+ getAppName(ctx)+"悬浮窗权限", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + ctx.getPackageName()));
                        ctx.startActivity(intent);
                    } else {
                        setSystemToast();
                    }
                } else {
                    setSystemToast();
                }
            }
            show();
            return new DialogTextViewBuilder(this);
        }

        /**
         * 设置为系统通知
         */
        private void setSystemToast(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);//设定为系统级警告
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4至6.0判断权限是否打开
                //AppOpsManager添加于API 19
                if (!checkAlertWindowsPermission(ctx)) {
                    applyCommonPermission(ctx);
                    Toast.makeText(ctx, "请打开"+getAppName(ctx)+"悬浮窗权限", Toast.LENGTH_LONG).show();
                }
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设定为系统级警告
            } else {
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设定为系统级警告
            }
        }

        private void setButton() {
            tvPrimary.setText(oneButton);
            if (twoButton != null) {
                tvSecondary.setText(twoButton);
            }
            tvSecondary.setVisibility(twoButton == null ? View.GONE : View.VISIBLE);
        }

        private void setView(int view) {
            if (view == -100) {
                setContentView(R.layout.dialog_base_textview);
            } else {
                setContentView(view);
            }

            Window window = getWindow();
            if (window != null) {
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.width = (int) (getScreenWidth(getContext()) * 0.85);
                attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(attributes);
            }

            tvTitle = findViewById(R.id.tvTitle);
            tvTitle.setText(title);
            tvContent = findViewById(R.id.tvContent);
            tvContent.setText(content);
            tvPrimary = findViewById(R.id.tvPrimary);
            tvSecondary = findViewById(R.id.tvSecondary);
            setTextViewBold(tvPrimary);
            setTextViewBold(tvSecondary);
            tvPrimary.setOnClickListener(this);
            tvSecondary.setOnClickListener(this);

            tvTitle.setVisibility(title != null && !title.equals("") ? View.VISIBLE : View.GONE);
            tvContent.setVisibility(content != null && !content.equals("") ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.tvPrimary) {
                if (listener == null) {
                    dismissDialog();
                } else {
                    if (!isClose) {
                        // 点击按钮不关闭对话框
                        listener.oneClick();
                    } else {
                        // 点击按钮回调并关闭对话框
                        listener.oneClick();
                        dismissDialog();
                    }
                }
            } else if (id == R.id.tvSecondary) {
                if (listener == null) {
                    dismissDialog();
                } else {
                    if (!isClose) {
                        // 点击按钮不关闭对话框
                        listener.twoClick();
                    } else {
                        // 点击按钮回调并关闭对话框
                        listener.twoClick();
                        dismissDialog();
                    }
                }
            }
        }

        private void dismissDialog(){
            dismiss();
        }
    }

    /**
     * 判断悬浮窗口权限是否打开
     *
     * @param context
     * @return true 允许  false禁止
     */
    private static boolean checkAlertWindowsPermission(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 6.0以下到4.4之间打开悬浮窗权限设置页面
     *
     * @param context
     */
    private static void applyCommonPermission(Context context) {
        try {
            Class clazz = Settings.class;
            Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
            Intent intent = new Intent(field.get(null).toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "请进入设置页面打开" + getAppName(context) + "悬浮窗权限！", Toast.LENGTH_LONG).show();
        }
    }

    private static String getAppName(Context context) {
        PackageManager packageManagers = context.getPackageManager();
        try {
            String appName = (String) packageManagers.getApplicationLabel(packageManagers.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA));
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 设置textview加粗
     *
     * @param textView
     */
    public static void setTextViewBold(TextView textView) {
        if (textView == null) {
            return;
        }
        TextPaint tp = textView.getPaint();
        tp.setFakeBoldText(true);
        textView.postInvalidate();
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return 宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        return screenWidth;
    }

    public interface DialogOnClickListener {
        void oneClick();

        void twoClick();
    }

    public interface DialogChoiceListener {

        void onChoice(DialogInterface dialog, int position, String s);
    }

    public static class ListenerRealize implements DialogOnClickListener{

        @Override
        public void oneClick() {

        }

        @Override
        public void twoClick() {

        }
    }
}