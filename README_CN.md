![logo](images/logo.png)

![frame](images/frame.png)


# 安全键盘
[![version](https://img.shields.io/badge/%E7%89%88%E6%9C%AC-0.1.0-brightgreen.svg)](https://bintray.com/geyifeng/maven/immersionbar) [![author](https://img.shields.io/badge/%E4%BD%9C%E8%80%85-hcxc-orange.svg)](https://github.com/gyf-dev)

## 项目介绍
此库提供了一个安全键盘。

账号，密码，短信验证码等关系到用户隐私、财产安全的信息均通过键盘输入到应用程序中。
一些不法分子可以通过挂钩系统输入键盘、第三方键盘，截获屏幕按键点击位置,dump内存等方式窃取以上信息，
威胁到用户的隐私财产安全。

为了解决这一问题，我们基于Android提供的KeyboardView和Keyboard，提供了一个安全键盘。
我们的安全键盘通过java代码绑定到EditText，根据EditText的焦点状态进行显示和隐藏。
当EditText获取焦点时，通过PopupWindow弹出安全键盘。

安全键盘按键包括数字和大小写字母。
按键序列使用AES加密后存储到内存中，以防止攻击者dump内存获取按键序列。
我们还提供了按键乱序功能，防止攻击者通过监听屏幕点击位置获取输入内容。
用户可以自定义键盘的标题和完成按钮文字。

## 效果图
<img width="150"  src="./images/default.png"/>
<img width="150"  src="./images/default1.png"/>

## 下载demo
#### [点我下载demo](./output/demo.apk)
#### [点我下载aar](./output/securekeyboard-release.aar)

## 版本说明
#### [点我查看版本说明](https://github.com/gyf-dev/ImmersionBar/wiki)


## 技术文档

### 1.基本用法
   ```kotlin
    fun bindSecureEditText(
        view: EditText,
        shuffle: Boolean?,
        title: String?,
        done: String?
    );
   ```
    shuffle: 是否乱序显示键盘按键
    title: 键盘标题，默认是"Secure Keyboard"
    done: 键盘完成按键文字，默认是"Done"

    使用:在Activity中使用SecureKeyboardManager绑定EditText
   ```xml
    <EditText
        android:id="@+id/et_shuffle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
   ```
   ```kotlin
    import com.hcxc.securekeyboard.SecureKeyboardManager
    SecureKeyboardManager.bindSecureEditText(findViewById(R.id.et_shuffle), true, "title", "done")
   ```

### 2.特别说明（参考修改和第三方库说明）
   ```kotlin
    import android.android.keyboard.Keyboard
    import android.android.keyboard.KeyboardView
   ```
    我们参考了Android的键盘并且将其转化为kotlin代码

