# Firebase 后续接入说明

当前项目已经声明 Firebase Authentication 和 Firebase Realtime Database 依赖，
但刻意没有放入 `google-services.json`，也没有实现真实登录或数据读写。

后续接入时按以下顺序操作：

1. 在 Firebase Console 创建或选择一个 Firebase 项目。
2. 注册 Android 应用，包名必须填写 `com.groupproject.justeat`。
3. 下载 `google-services.json`，放到 `app/google-services.json`。
4. 重新执行 Gradle Sync。`app/build.gradle.kts` 检测到该文件后会自动应用
   Google Services Gradle 插件。
5. 在 Firebase Authentication 中启用 Email/Password 登录方式。
6. 创建 Firebase Realtime Database，并在写入任何真实数据之前定义安全规则。
7. 在新的 `data/repository/firebase` 包内实现现有 Repository 接口；页面和
   ViewModel 只依赖接口，不直接访问 Firebase SDK。
8. 不要提交 `google-services.json`，也不要把明文密码写入 Realtime Database、
   日志、SharedPreferences 或任何业务实体。

建议先为开发和演示环境使用独立 Firebase 项目。接入 Auth 后，把
`LoginViewModel` 的本地成功状态替换成 `AuthRepository` 的异步结果，同时保留
现有输入校验作为网络请求前的第一层检查。

## 安全提醒

- Realtime Database 默认测试规则不适合提交作业或部署；应限制为已认证用户，
  并根据 `UserRole` 控制 Customer、Staff、Manager 的读写范围。
- 客户端角色字段不能作为唯一授权依据，关键权限必须由数据库安全规则校验。
- Firebase Authentication 管理密码；应用自己的数据库中只保存 Firebase UID
  和必要的个人资料字段。
- 使用真实账号测试前，确认日志中不会输出邮箱、Token、密码或完整用户资料。

