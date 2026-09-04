# Just Eat 团队开发规则

本文档是项目的团队协作规则。所有成员提交到 `main` 前都应满足这些要求。

## 1. 基本原则

1. 只实现当前任务范围，避免在一个 Pull Request 中混入无关重构。
2. UI、业务状态和数据访问分层；任何层都不能绕过既定边界。
3. 新行为必须可验证，错误路径与成功路径同等重要。
4. Firebase 权限采用最小权限原则，不能依赖客户端界面隐藏来保护数据。
5. 密码、Token、密钥和配置凭据不得写入代码、日志或 Git 历史。

## 2. 分支与提交

### 2.1 分支命名

从最新 `main` 创建短生命周期分支：

```text
feature/customer-menu
feature/table-booking
fix/login-validation
docs/firebase-rules
test/order-viewmodel
```

不允许长期在个人分支积累多个无关功能。合并前先同步 `main` 并解决冲突。

### 2.2 Commit 信息

使用英文 Conventional Commit 格式：

```text
feat: add customer menu list
fix: prevent duplicate booking submission
test: cover order status transitions
docs: update Firebase setup guide
refactor: extract booking validator
```

每个 commit 应保持可编译，并只表达一个主要目的。禁止使用 `update`、`changes`、
`final` 等无法说明内容的提交信息。

### 2.3 Pull Request

- `main` 只接收经过审查的完整功能或修复。
- PR 描述必须包含：目的、主要改动、测试方法、截图（涉及 UI 时）和已知限制。
- 至少一名其他成员完成审查后再合并。
- 作者必须处理 Critical 和 Important 问题；Minor 问题可记录后续任务。
- 禁止通过 force push 或重写共享 `main` 历史解决普通冲突。

## 3. Java 规则

- 生产代码使用 Java 17，不在项目中混入 Kotlin。
- 一个 `.java` 文件只放一个 public 顶层类。
- 类名使用 PascalCase，方法和变量使用 camelCase，常量使用 UPPER_SNAKE_CASE。
- 方法保持单一职责；复杂分支优先拆成可命名、可测试的小方法。
- 对外 API、基类、实体字段和非显而易见规则使用中文 Javadoc/注释。
- 不保留未使用代码、注释掉的大段代码或无负责人 TODO。
- 不捕获异常后静默忽略；应转换为明确的 UI 状态或 Repository 错误。
- 不在 Activity、ViewModel 或 Repository 中记录密码和 Token。

## 4. Android UI 规则

- Activity 必须继承 `BaseActivity<VB>` 并使用 ViewBinding。
- DialogFragment 必须继承 `BaseDialog<VB>` 并使用 Fragment arguments 传参。
- 生产页面禁止使用 `findViewById`；测试代码可以用于取得 Android content root。
- Activity 只绑定操作和渲染状态，不直接调用 Firebase 或包含业务规则。
- 所有用户可见文字放入 `strings.xml`，禁止硬编码在 Java 或布局中。
- 可点击控件目标尺寸至少为 48dp，并提供明确的 enabled/loading 状态。
- 有意义的图片必须提供 `contentDescription`；纯装饰图片明确设为无障碍忽略。
- 表单错误显示在对应输入框，提交失败后聚焦第一个错误字段。
- 一次性 Dialog、Toast 和导航事件展示后必须消费，并使用稳定 tag 去重。
- 布局应支持小屏、键盘弹出和横竖屏；长表单使用可滚动容器。

## 5. ViewModel 与状态规则

- ViewModel 不持有 Activity、Fragment、View、ViewBinding、Dialog 或 Context。
- UI 使用只读 `LiveData` 观察状态，MutableLiveData 保持 private。
- UiState 应不可变，并能完整描述当前页面需要渲染的状态。
- 每次异步操作都必须正确结束 loading，包括失败和提前返回路径。
- Repository 回调先转换成 ViewModel 状态，再由 Activity 渲染。
- 旋转或重建后不应重复提交网络请求或重复弹出已经消费的提示。

## 6. Repository 与 Firebase 规则

- Activity 和 Dialog 禁止直接引用 `FirebaseAuth`、`DatabaseReference` 或
  `DataSnapshot`。
- Firebase 实现只放在 `data/repository/firebase`。
- 上层依赖现有 Repository 接口；确需修改接口时同步更新实现、测试和文档。
- 数据读取和写入必须处理成功、权限失败、网络失败和数据格式错误。
- 多节点相关写入使用原子 multi-location update，避免半完成状态。
- 上线前使用 Firebase Emulator Suite 测试安全规则。
- 数据库禁止开放全局 `.read: true` 或 `.write: true`。
- 角色权限必须由 Security Rules 验证，不能信任客户端传入的 role。

## 7. 实体与数据规则

每个 Firebase 实体必须同时具备：

- public 无参构造方法。
- 包含全部字段的 public 构造方法。
- 每个字段的 getter 和 setter。
- 类、字段、构造方法和特殊约束的中文注释。

统一数据约定：

- 金额使用 `long` cents，禁止使用 `float` 或 `double` 保存价格。
- 时间使用 `long` Unix epoch milliseconds。
- ID 使用 `String`，并保持 Firebase 路径键与实体 ID 一致。
- 状态值来自 `domain/constant`，禁止在页面中散落魔法字符串。
- 历史订单保存菜品名称和价格快照，不能依赖当前菜单价格还原历史。
- 实体不包含密码、认证 Token、Android View 或 Firebase SDK 对象。

## 8. 测试规则

- 新功能或修复先添加能失败的测试，再编写生产实现。
- Validator 必须覆盖 null、空白、边界和成功路径。
- ViewModel 必须覆盖初始、loading、成功、错误和一次性事件。
- 新实体必须测试无参构造、全字段构造及全部 getter/setter。
- UI 关键流程使用 Robolectric 或设备测试，并覆盖最低支持 API。
- 修复崩溃时保留可防止问题复发的测试或可重复冒烟步骤。

提交前至少运行：

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

## 9. 安全与隐私红线

以下内容禁止提交或记录：

- `google-services.json`
- keystore、签名密码和服务账号密钥
- Firebase/API Token
- 用户密码
- 真实用户的完整个人资料或生产数据库导出

发现凭据误提交时，应立即停止共享、撤销或轮换凭据，并清理 Git 历史；只删除
最新文件并不能使已经进入历史的密钥失效。

## 10. 审查检查表

审查者至少检查：

- [ ] 功能符合 issue/任务范围。
- [ ] 分层和依赖方向正确。
- [ ] 生命周期与配置变更处理正确。
- [ ] Firebase 权限和错误路径安全。
- [ ] 没有密码、Token 或凭据泄漏。
- [ ] 实体和数据库字段保持兼容。
- [ ] UI 文字、无障碍和小屏行为合理。
- [ ] 测试验证真实行为，而不是只验证 mock 调用。
- [ ] 文档与代码同步。

## 11. Definition of Done

一个任务只有同时满足以下条件才算完成：

1. 验收条件全部实现，没有越出任务范围。
2. 代码位于正确分层，没有已知 Critical 或 Important 审查问题。
3. 新行为有测试，完整测试为零失败。
4. Lint 没有新增错误；新增警告有明确原因。
5. Debug APK 可构建，相关页面完成真机或模拟器冒烟测试。
6. 没有提交敏感文件，日志不包含敏感信息。
7. README、开发指南、Firebase 说明或规则已按需更新。
8. PR 已由至少一名其他成员审查并合并。

## 12. 争议处理

遇到实体字段、数据库路径、状态名称、Repository 接口或架构方向分歧时，不要让
每个分支各自实现。先在 issue/小组记录中写明选项和影响，由全组确认后更新文档，
再继续编码。已写入本规范的规则优先于个人习惯；确需改变时通过独立 PR 修改规范。
