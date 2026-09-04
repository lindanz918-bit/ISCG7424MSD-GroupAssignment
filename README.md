# Just Eat Android 基础框架

这是根据课程 proposal 搭建的 Java/XML Android 基础项目，包名为
`com.groupproject.justeat`。当前交付只实现登录页的界面、本地输入校验和
ViewModel 状态流，不包含完整业务页面，也不会执行真实 Firebase 登录。

## 当前包含

- 单 `app` 模块的轻量 MVVM 分层
- `LoginActivity`、`LoginViewModel`、`LoginUiState` 和 `LoginValidator`
- ViewBinding 版 `BaseActivity` 与 `BaseDialog`
- 可复用的 `MessageDialog`
- Glide 图片加载封装 `GlideImageLoader`
- Firebase Authentication / Realtime Database 依赖和 Repository 接口
- 用户、菜单、餐桌、预订、订单等 7 个完整 Java 实体类
- Firebase 友好的角色与状态常量
- JUnit / Robolectric 自动化测试

登录按钮当前只检查邮箱格式和密码长度。校验成功表示输入符合规则，不表示
用户已通过 Firebase Authentication 验证，也不会跳转到其他页面。

## 开发环境

- Android Studio（支持 AGP 8.9.3）
- JDK 17
- Android SDK 35
- 最低支持 Android 7.0（API 24）

用 Android Studio 打开本目录，等待 Gradle Sync 完成即可运行。项目暂时不需要
`google-services.json`；加入该文件后的步骤见
[`docs/FIREBASE_SETUP.md`](docs/FIREBASE_SETUP.md)。

## 常用命令

在项目根目录执行：

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Debug APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 代码结构

```text
com.groupproject.justeat/
├── base/                 # Activity、Dialog、ViewModel 基类
├── data/
│   ├── model/            # Firebase 兼容的业务实体
│   └── repository/       # 数据访问接口，暂不含 Firebase 实现
├── domain/constant/      # 角色和业务状态常量
├── feature/login/        # 当前唯一完成的 LoginActivity 功能
├── ui/dialog/            # 通用 MessageDialog
└── util/                 # Glide 图片加载等工具
```

后续页面建议分别放入 `feature/customer`、`feature/staff` 和
`feature/manager`，Firebase 实现放入 `data/repository/firebase`。

## 重要约定

- 页面和 Dialog 使用 ViewBinding，不在生产页面中使用 `findViewById`。
- 金额使用 `long` 类型的分（cents），避免浮点误差。
- 时间使用 `long` 类型的 Unix epoch milliseconds。
- 实体类保留公开无参构造方法，以支持 Firebase 反序列化。
- 不记录、不持久化密码；密码只在提交校验时短暂传入 ViewModel。
- `app/google-services.json` 已加入 `.gitignore`，不要提交到 Git。

