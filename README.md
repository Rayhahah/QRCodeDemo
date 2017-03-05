# QRCodeDemo

[toc]
>[博客地址]()
>需要的资源可以直接从这个项目里面提取

#新手快速集成ZXing的扫描二维码，同时自定义封装实现

##基本概述
>最老牌的二维码框架
###优势
- Google的开源框架，高度的可定制性
- 除了二维码还可以识别其他码，如条形码
- 不依赖第三方，使用简单
###缺点
- 相对Gradle依赖，ZXing的集成更为繁琐
- 高度的可定制性也表示这更高的学习成本

##开发过程
###ZXing集成
####导入ZXing.jar包
![Alt text](./导入ZXing.jar.png)
- 导入以后记得右键add as library
####拷贝资源文件到工程中
将下面资源拷贝到自己工程下
- layout包：
	- `activity_qrcode_capture_layout.xml` ：主要扫码界面的布局，自定义布局的核心文件

- raw包：（没有就新建）
	- `beep.ogg` :这个就是扫码成功的提示音效，也可以用自己的音效，不过名字要一样

- values包：
	- 在`colors.xml`中添加如下的资源：
 

```
<!-- QR_Code -->
    <color name="viewfinder_mask">#60000000</color>
    <color name="result_view">#b0000000</color>
    <color name="possible_result_points">#c0ffff00</color>
    <color name="result_image_border">#ffffffff</color>
    <color name="result_minor_text">#ffc0c0c0</color>
    <color name="result_points">#c000ff00</color>
    <color name="system_bar_color">#fed952</color>
```
	
	
-  在`ids.xml`下添加如下资源：（没有就新建）

```
 <!-- QR_Code -->
    <item name="auto_focus" type="id"/>
    <item name="decode" type="id"/>
    <item name="decode_failed" type="id"/>
    <item name="decode_succeeded" type="id"/>
    <item name="encode_failed" type="id"/>
    <item name="encode_succeeded" type="id"/>
    <item name="launch_product_query" type="id"/>
    <item name="quit" type="id"/>
    <item name="restart_preview" type="id"/>
    <item name="return_scan_result" type="id"/>
    <item name="search_book_contents_failed" type="id"/>
    <item name="search_book_contents_succeeded" type="id"/>
    <item name="gridview" type="id"/>
    <item name="webview" type="id"/>
    <item name="about_version_code" type="id">false</item>
    <item name="split" type="id">false</item>
```

- `strings.xml` 资源添加一下字符串资源：

```
   <!-- QR_Code -->
    <string name="button_ok">OK</string>
    <string name="msg_camera_framework_bug">Sorry, the Android camera encountered a problem. You may need to restart the
        device.
    </string>
    <string name="msg_bulk_mode_scanned">Bulk mode: barcode scanned and saved</string>
    <string name="scan_text">放入框中即可进行二维码扫描</string>
    <string name="contents_contact">Contact info</string>
    <string name="contents_email">Email address</string>
    <string name="contents_location">Geographic coordinates</string>
    <string name="contents_phone">Phone number</string>
    <string name="contents_sms">SMS address</string>
    <string name="contents_text">Plain text</string>
    <string name="preferences_vibrate">preferences_vibrate</string>
    <string name="preferences_play_beep">preferences_play_beep</string>
    <string name="preferences_actions_title">When a barcode is found\u2026</string>
    <string name="preferences_copy_to_clipboard_title">Copy to clipboard</string>
    <string name="preferences_decode_1D_title">1D barcodes</string>
    <string name="preferences_decode_Data_Matrix_title">Data Matrix</string>
    <string name="preferences_decode_QR_title">QR Codes</string>
    <string name="preferences_play_beep_title">Beep</string>
    <string name="preferences_remember_duplicates_summary">Store multiple scans of the same barcode in History</string>
    <string name="preferences_remember_duplicates_title">Remember duplicates</string>
    <string name="preferences_scanning_title">When scanning for barcodes, decode\u2026</string>
    <string name="preferences_supplemental_summary">Try to retrieve more information about the barcode contents</string>
    <string name="preferences_supplemental_title">Retrieve more info</string>
    <string name="preferences_vibrate_title">Vibrate</string>
    <string name="preview_msg">&#160;preview pages available,click Download for more.</string>
```

- xml包： 新建 xml包
	- 添加：`preferences.xml`

####拷贝扫码核心类到工程中
这个过程很简单，直接将ZXing的Java核心类文件夹拷贝到你的工程包名目录下就可以了
![Alt text](./ZXing文件目录导入.png)

###主要包类简述
>对核心功能类的了解，就是我们自定义扫码功能的基础

- `CaptureActivity` ：整个扫码的界面活动类
- `PreferencesActivity`  ： 扫码功能的配置类，**不会**对其作出修改
- `CameraManager`  ：
	- 扫码的预览闪光灯等一系列功能，主要提供给我们调用，也不会对它作出修改
	- Camera类中暴露其他Manager的实现接口而已

- `DecodeThread`  ： 解码的线程
- `DecodeHandler`   :  中转类，将线程的消息转发到 下面的CaptureActivityHandler
- `CaptureActivityHandler`   ： 异步回传消息真正的逻辑处理实现类
- `Util` ：获取屏幕宽高和IMEI
- `ViewfinderView`  **扫码自定义框最主要的类**，自定义扫码框主要在这里完成

###自定义扫码页面布局文件
主要在`activity_qrcode_capture_layout.xml`中操作：
本来的文件布局：
只有一个简单的摄像头预览的SurfaceView和扫码框ViewfinderView

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent">

    <RelativeLayout
        android:layout_width="fill_parent"
        android:layout_height="fill_parent">

        <SurfaceView
            android:id="@+id/preview_view"
            android:layout_width="fill_parent"
            android:layout_height="fill_parent"
            android:layout_gravity="center" />

        <com.youdu.zxing.view.ViewfinderView
            android:id="@+id/viewfinder_view"
            android:layout_width="fill_parent"
            android:layout_height="fill_parent" />


   <!-- 自定义的布局界面-->
  <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical">

            <include
                android:id="@+id/include1"
                layout="@layout/layout_qrcode_toolbar"
                android:layout_width="match_parent"
                android:layout_height="@dimens/dp_big_ii" />

            <View
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1" />

            <include
                android:id="@+id/include2"
                layout="@layout/layout_qrcode_bottom_feature"
                android:layout_width="match_parent"
                android:layout_height="@dimens/dp_big_ii" />

        </LinearLayout>


    </RelativeLayout>

</FrameLayout>
```

然后加入我们自己的元素：
1. titleBar : 中间的标题TextView、 返回键

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="?android:attr/actionBarSize">

    <TextView
        android:id="@+id/qrcode_btn_back"
        android:layout_width="@dimen/dp_middle_i"
        android:layout_height="@dimen/dp_middle_i"
        android:layout_centerVertical="true"
        android:layout_marginLeft="@dimen/dp_small_iiii"
        android:background="@drawable/selector_ic_back" />

    <TextView
        style="@style/text_big_center"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:text="扫一扫"
        android:textColor="@color/white" />

</RelativeLayout>
```

2. 底部功能栏：相册、闪光的、生成二维码 

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="75dp"
    android:orientation="horizontal">

    <TextView
        android:id="@+id/qrcode_btn_photo"
        style="@style/text_middle_center"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:drawableTop="@drawable/selector_ic_photo"
        android:text="相册"
        android:textColor="@color/white" />

    <TextView
        android:id="@+id/qrcode_btn_flash"
        style="@style/text_middle_center"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:drawableTop="@drawable/selector_ic_flash"
        android:text="闪光灯"
        android:textColor="@color/white" />

    <TextView
        android:id="@+id/qrcode_btn_encode"
        style="@style/text_middle_center"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:drawableTop="@drawable/selector_ic_qrcode"
        android:text="二维码"
        android:textColor="@color/white" />

</LinearLayout>
```


扫码框的自定义实现主要在`ViewfinderView.java`中onDraw动态绘制：
主要原理就是获取他本来的扫码框大小，然后在他的四个角分别绘制两个长方形来包裹，同时在中间实现一个不断移动的扫码条


```
 @Override
    public void onDraw(Canvas canvas) {
    //获取得到扫码框矩形的大小
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }

        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
            slideBottom = frame.bottom;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        //框架本身的扫码框绘制
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
                paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

//自定义部分开始
        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {

			//根据扫码框的大小来绘制
            //画出8个正方形，组成括住扫码框
            paint.setColor(Color.BLUE);
            canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH,
                    frame.top + ScreenRate, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
                    frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right,
                    frame.top + ScreenRate, paint);
            canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
                    + ScreenRate, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left
                    + CORNER_WIDTH, frame.bottom, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.bottom
                    - CORNER_WIDTH, frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom
                    - ScreenRate, frame.right, frame.bottom, paint);

            /**
             * 一直在移动的扫描条
             */
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= frame.bottom) {
                slideTop = frame.top;
            }
            Rect lineRect = new Rect();
            lineRect.left = frame.left;
            lineRect.right = frame.right;
            lineRect.top = slideTop;
            lineRect.bottom = slideTop + 18;
            canvas.drawBitmap(((BitmapDrawable) (getResources()
                            .getDrawable(R.drawable.fle))).getBitmap(), null, lineRect,
                    paint); //扫描条的资源
//扫码框下方的文字提示
            paint.setColor(Color.WHITE);
            paint.setTextSize(TEXT_SIZE * density);
            paint.setAlpha(0x40);
            paint.setTypeface(Typeface.create("System", Typeface.BOLD));
            String text = getResources().getString(R.string.scan_text);  //文字资源
            float textWidth = paint.measureText(text);

            canvas.drawText(
                    text,
                    (width - textWidth) / 2,
                    (float) (frame.bottom + (float) TEXT_PADDING_TOP * density),
                    paint);
//自定义部分结束

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top
                            + point.getY(), 6.0f, paint);
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top
                            + point.getY(), 3.0f, paint);
                }
            }

            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }
    }
```

###自定义功能代码
在`CaptureActivity.Java`中完成：
1. 首先找到我们的控件：

```
 btnBack = ((TextView) findViewById(R.id.qrcode_btn_back));
        btnPhoto = ((TextView) findViewById(R.id.qrcode_btn_photo));
        btnFlash = ((TextView) findViewById(R.id.qrcode_btn_flash));
        btnEncode = ((TextView) findViewById(R.id.qrcode_btn_encode));
        btnBack.setOnClickListener(this);
        btnPhoto.setOnClickListener(this);
        btnFlash.setOnClickListener(this);
        btnEncode.setOnClickListener(this);
```

2. 监听实现：

```
 @Override
    public void onClick(View view) {
        switch (view.getId()) {
		        //返回按钮
            case R.id.qrcode_btn_back:
                finish();
                break;
                //选择本地图片
            case R.id.qrcode_btn_photo:
                //跳转到系统相册选择图片
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                Intent wrapperIntent = Intent.createChooser(intent, "选择二维码图片");
                //开启本地图片选择，并携带获取回传值
                startActivityForResult(wrapperIntent, REQUEST_CODE);
                break;
                //开启闪光灯，主要调用CameraManager封装好的方法
            case R.id.qrcode_btn_flash:
                if (isFlash) {
                    //关闭闪光灯
                    CameraManager.get().turnLightOff();
                } else {
                    //开启闪光灯
                    CameraManager.get().turnLightOff();
                }
                break;
                //生成二维码页面
            case R.id.qrcode_btn_encode:
                // 跳转到生成二维码页面
                Bitmap b = createQRCode();
                Intent intentEncode = getIntent();
                intentEncode.putExtra("QR_CODE", b);
                setResult(200, intentEncode);
                finish();
                break;
        }
    }
```

3. 在`CaptureActivity`中获取相册中选择的图片来扫码：

```
  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回选择的需要扫描二维码的图片
        if (resultCode == RESULT_OK) {
            //被选择的二维码图片的uri
            uri = data.getData();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //扫描二维码图片，得到结果
                    //这个是ZXing帮我们封装好的方法了
                    Result result = scanningImage(uri);
                    if (result == null) {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "图片格式有误", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else {
                        // 数据返回，给我们的启动Activity，如：MAinActivity
                        String recode = (result.toString());
                        Intent data = new Intent();
                        data.putExtra("result", recode);
                        setResult(300, data);
                        finish();
                    }
                }
            }).start();
        }
    }
```

4. 最后只要在我们启动的Activity中的获取二维码扫描后的内容并做自己的处理就可以了，MainActivity中的使用

```
 private static final int REQUEST_QRCODE = 0x01;

    public void onQRCodeClick(View view) {
        //启动二维码扫描的页面功能
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_QRCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            //返回的二维码解析数据
            case REQUEST_QRCODE:
                if (resultCode == Activity.RESULT_OK) {
                    String code = data.getStringExtra("result");
                    //下面就是自己对二维码数据的解析了
                    //........
                }
                break;
        }
    }

```


