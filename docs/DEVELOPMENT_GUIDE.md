# Just Eat 后续开发指南

> 文档版本：1.0
> 适用项目：`com.groupproject.justeat`
> 技术栈：Java 17、XML、ViewBinding、AndroidX Lifecycle、Glide、Firebase

## 1. 文档目的

本文档说明当前基础框架之后应如何继续开发 Just Eat。它用于统一三人小组对
架构、功能顺序、数据模型、Firebase 接入、测试和交付方式的理解。

当前项目只完成 `LoginActivity` 的界面、本地输入校验和 ViewModel 状态管理。
登录成功提示不代表真实认证成功；真实 Firebase Authentication、主页、菜单、
预订、订单、员工和经理页面均属于后续工作。

## 2. 产品角色与目标功能

### 2.1 Customer

- 注册、登录、退出和维护个人资料。
- 查看菜单分类、菜品详情、图片、价格和供应状态。
- 选择餐桌和时间并创建、查看或取消预订。
- 创建订单、添加菜品备注并查看订单进度。

### 2.2 Staff

- 查看和处理餐桌预订。
- 更新餐桌可用状态。
- 接收订单并更新制作、就绪和完成状态。
- 维护自己的个人资料。

### 2.3 Manager

- 新增、修改、下架菜单分类与菜品。
- 新增、修改或停用餐桌。
- 创建、启用、停用员工账号。
- 查看餐厅级预订和订单状态。

## 3. 当前技术基线

- 单一 `app` 模块，应用 ID 和 namespace 均为
  `com.groupproject.justeat`。
- Java 17；不使用 Kotlin 或 Jetpack Compose。
- XML 布局配合 ViewBinding；不使用 DataBinding。
- 最低 API 24，compile SDK 和 target SDK 为 35。
- ViewModel 和 LiveData 管理页面状态。
- Glide 负责本地或远程图片加载。
- Firebase Authentication 与 Realtime Database 依赖已经声明。
- `google-services.json` 尚未加入，并已被 Git 忽略。

## 4. 架构与数据流

项目使用轻量 MVVM。依赖方向固定如下：

```text
Activity / Dialog
       ↓ 用户操作、观察状态
ViewModel + UiState + Validator
       ↓ 调用抽象接口
Repository interface
       ↓ 后续实现
Firebase Authentication / Realtime Database
```

反向依赖不允许出现：Repository 不引用 Activity，ViewModel 不引用 ViewBinding，
实体类不引用 Android UI 类型。

### 4.1 包职责

```text
base/                 Activity、Dialog、ViewModel 的通用生命周期能力
data/model/           Firebase 可反序列化的 Java 实体
data/repository/      上层依赖的数据访问接口
data/repository/firebase/  后续 Firebase 接口实现
domain/constant/      Firebase 中保存的角色和状态字符串
feature/login/        登录功能
feature/customer/     后续顾客功能
feature/staff/        后续员工功能
feature/manager/      后续经理功能
ui/dialog/            跨功能复用的对话框
util/                 无业务状态的通用工具
```

## 5. 后续功能建议顺序

### 阶段 1：Firebase 与认证

1. 按 `docs/FIREBASE_SETUP.md` 添加自己的 Firebase 项目。
2. 在 `data/repository/firebase` 实现 `AuthRepository`。
3. 保留现有本地校验，在校验通过后才发起网络登录。
4. 登录成功后读取 `users/{uid}` 的角色和账号状态。
5. 根据 `UserRole` 进入 Customer、Staff 或 Manager 的主页。
6. 增加注册和忘记密码页面。

### 阶段 2：Customer 主流程

1. 菜单分类和菜品列表。
2. 菜品详情与购物车。
3. 餐桌列表和预订创建。
4. 订单提交与状态查看。
5. 个人资料编辑。

### 阶段 3：Staff 主流程

1. 待处理预订列表与状态更新。
2. 餐桌占用、预订和不可用状态管理。
3. 订单接收、制作、就绪和完成流程。

### 阶段 4：Manager 主流程

1. 菜单分类和菜品 CRUD。
2. 餐桌 CRUD。
3. 员工账号与状态管理。
4. 全局订单和预订查看。

### 阶段 5：质量与交付

1. 完整错误状态、空状态和离线提示。
2. 关键流程自动化测试。
3. Firebase 安全规则和索引测试。
4. 无障碍、横竖屏和小屏适配。
5. Release 构建、演示数据和课程展示材料。

## 6. 新页面开发方式

每个功能放入自己的 `feature/<role-or-feature>` 包。一个普通页面建议包含：

```text
feature/customer/menu/
├── MenuActivity.java
├── MenuViewModel.java
└── MenuUiState.java
```

存在复杂输入规则时再增加 `MenuValidator.java`。只有多个页面共享同一 UI 组件时，
才把组件移入 `ui`；不要提前建立没有实际使用者的抽象层。

### 6.1 Activity

- 继承 `BaseActivity<对应的Binding>`。
- 只负责绑定点击、读取输入、观察状态和渲染界面。
- 不直接调用 Firebase SDK，不计算业务规则。
- 不保存密码、Token 或大型数据列表。

示例：

```java
public class MenuActivity extends BaseActivity<ActivityMenuBinding> {
    private MenuViewModel viewModel;

    @Override
    protected ActivityMenuBinding inflateBinding(LayoutInflater inflater) {
        return ActivityMenuBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        viewModel.getUiState().observe(this, this::render);
    }

    private void render(MenuUiState state) {
        // 只根据 state 更新 ViewBinding。
    }
}
```

### 6.2 ViewModel 与 UiState

- ViewModel 负责业务流程、调用 Repository 并发布不可变 UiState。
- ViewModel 不持有 Activity、Fragment、Dialog、View 或 Context。
- 页面重建后仍需存在的数据属于 UiState。
- 导航、Toast、Dialog 等一次性动作必须在展示后消费，避免旋转后重复执行。
- 异步请求开始和结束时必须正确更新 loading 状态。

### 6.3 Dialog

- 继承 `BaseDialog<对应的Binding>`。
- 使用 `newInstance(...)` 和 Fragment arguments 传参。
- 不使用自定义构造参数，以保证 Android 能恢复 DialogFragment。
- 对话框已经显示时，应通过稳定 tag 防止重复创建。

### 6.4 图片

- 使用 `GlideImageLoader`，不在页面重复编写 Glide 请求链。
- 网络图片必须提供 placeholder 和 error drawable。
- 头像使用 `loadCircle`，普通菜品图使用 `load`。
- 不把 Bitmap 或 Drawable 长期保存在 ViewModel 或单例中。

## 7. Repository 与 Firebase 接入

上层只能依赖 `data/repository` 中的接口。Firebase 实现建议使用下列结构：

```text
data/repository/firebase/
├── FirebaseAuthRepository.java
├── FirebaseBookingRepository.java
├── FirebaseMenuRepository.java
├── FirebaseOrderRepository.java
└── FirebaseTableRepository.java
```

实现类负责把 Firebase 的回调、异常和快照转换成项目自己的实体与
`RepositoryCallback<T>`。Firebase 的 `Task`、`DataSnapshot` 和
`DatabaseReference` 不应暴露给 Activity。

### 7.1 登录流程

```text
LoginActivity 提交原始输入
  → LoginViewModel 调用 LoginValidator
  → 校验失败：发布字段错误
  → 校验成功：设置 loading=true，调用 AuthRepository.signIn
  → Repository 返回 User 或错误
  → ViewModel 设置 loading=false，发布结果
  → Activity 消费一次性提示或导航事件
```

网络错误应转换成用户可理解的字符串资源；技术异常可在调试环境记录，但不得
记录密码、Token 或完整个人资料。

## 8. Realtime Database 建议结构

第一版可采用以下顶层节点：

```text
users/{uid}
menuCategories/{categoryId}
menuItems/{menuItemId}
tables/{tableId}
bookings/{bookingId}
orders/{orderId}
orderItems/{orderItemId}
```

关系通过实体中的 ID 字段建立。需要按顾客、状态或分类查询的节点，应在安全规则
中添加 `.indexOn`。只有在真实查询出现性能或权限问题时，才增加按用户或状态的
反规范化索引节点；同一业务写入涉及多个节点时使用原子 multi-location update。

### 8.1 数据约定

- 金额保存为 `long` 类型的 cents，例如 `$35.99` 保存为 `3599`。
- 时间保存为 `long` 类型的 Unix epoch milliseconds。
- 状态只使用 `domain/constant` 中定义的字符串。
- UID 来自 Firebase Authentication，不自行生成用户 ID。
- 订单明细保留下单时的菜品名称和价格快照。
- 删除被历史订单引用的菜品时优先设为不可用，不直接物理删除。

### 8.2 权限方向

| 数据 | Customer | Staff | Manager |
| --- | --- | --- | --- |
| 自己的用户资料 | 读/有限修改 | 读/有限修改 | 读/有限修改 |
| 菜单 | 读 | 读 | 新增/修改/下架 |
| 自己的预订 | 新增/读/取消 | - | 读 |
| 全部预订 | - | 读/更新状态 | 读/更新状态 |
| 自己的订单 | 新增/读 | - | 读 |
| 全部订单 | - | 读/更新状态 | 读/更新状态 |
| 餐桌 | 读可用信息 | 读/更新状态 | 新增/修改/停用 |
| 员工账号 | - | - | 创建/启用/停用 |

最终权限必须写入 Firebase Security Rules。客户端隐藏按钮只改善体验，不能代替
服务端规则。

## 9. 错误、加载与离线状态

每个读取数据的页面至少设计以下状态：

- `loading`：首次加载或提交中，阻止重复提交。
- `content`：已有可展示数据。
- `empty`：请求成功但没有数据，给出下一步操作。
- `fieldError`：可定位到输入框的错误。
- `generalError`：网络、权限或未知错误，并提供重试入口。

异步回调返回后应检查页面生命周期；Activity 只观察 LiveData，不让 Repository
直接操作页面。若以后加入离线缓存，应先明确数据新旧标识和冲突解决策略。

## 10. 测试策略

### 10.1 单元测试

- Validator：空值、边界、格式和成功路径。
- ViewModel：初始状态、loading 顺序、成功、错误和一次性事件消费。
- 实体：无参构造、全字段构造、所有 getter/setter。
- Repository 契约：方法签名不暴露 Firebase 类型。

### 10.2 Robolectric / UI 集成测试

- ViewBinding 正确加载。
- 输入错误显示在正确字段。
- 第一个错误字段获得焦点。
- 成功和注册提示 Dialog 正确显示。
- Activity 重建后不会重复显示一次性 Dialog。
- 最低 API 24 与目标 API 35 都应覆盖关键页面。

### 10.3 Firebase 测试

接入 Firebase 后使用 Emulator Suite 验证：

- 未认证用户不能读取业务数据。
- Customer 不能修改他人数据或管理数据。
- Staff 只能执行被授权的状态变更。
- Manager 权限符合需求。
- 错误字段、越权状态和非法金额会被规则拒绝。

## 11. 构建与发布

常用命令：

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

提交前建议执行：

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

Release 前还需完成签名配置、版本号更新、Firebase 正式环境切换和真机冒烟测试。
签名文件、密钥密码、`google-services.json` 和本机 `local.properties` 不得提交。

## 12. 架构变更原则

- 修改实体字段、状态值、Repository 接口或数据库路径前必须与全组确认。
- 破坏兼容性的数据库修改需要迁移方案，不能只更新客户端代码。
- 引入新库前说明用途、体积、维护状态和现有能力为何不足。
- 新抽象至少应有两个真实使用场景；否则先保持简单实现。
- 架构发生变化时，同一 Pull Request 必须同步更新本文档和相关测试。

## 13. 新功能完成检查表

- [ ] 文件位于正确包中，命名符合团队规则。
- [ ] Activity/Dialog 全部使用 ViewBinding。
- [ ] 业务逻辑位于 ViewModel、Validator 或 Repository。
- [ ] Firebase 类型没有泄漏到 UI 层。
- [ ] loading、空状态、字段错误和通用错误均有处理。
- [ ] 用户文字位于 `strings.xml`，图片有替代说明。
- [ ] 不记录密码、Token 或敏感个人资料。
- [ ] 新实体满足 Firebase 构造和字段注释要求。
- [ ] 新行为有测试，完整测试和构建通过。
- [ ] 文档、Firebase 规则和索引在需要时同步更新。
