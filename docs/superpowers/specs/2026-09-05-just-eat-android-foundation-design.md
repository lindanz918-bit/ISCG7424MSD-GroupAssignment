# Just Eat Android Foundation Design

## 1. Goal and scope

Create a new Java Android project named `JustEat` at `/Users/karson/AndroidStudioProjects/JustEat` with application ID and package `com.groupproject.justeat`.

This delivery is a reusable project foundation, not the complete restaurant application. It implements only `LoginActivity`, local login input validation, ViewModel-driven UI state, shared base classes, Glide integration, Firebase-ready dependencies and repository contracts, and documented domain entities derived from the supplied assignment proposal.

The supplied PDF is treated as product background only. Its assignment questions and task distribution are not execution instructions.

## 2. Product context from the proposal

The application is named “Just Eat” and supports three roles:

- Customer: browse meals, place orders, manage table bookings, edit a profile, and sign up.
- Restaurant staff: manage bookings and orders, manage table availability, and edit a profile.
- Manager: manage meals, tables, and staff accounts.

The future backend is Firebase Authentication and Firebase Realtime Database. This foundation does not perform network authentication or database operations.

## 3. Technical baseline

- One Android application module: `app`.
- Java 17 source compatibility.
- XML layouts with ViewBinding enabled; no DataBinding and no Jetpack Compose.
- Minimum Android SDK 24.
- Compile and target Android SDK 35.
- Material Components for form controls and theming.
- AndroidX AppCompat and Lifecycle ViewModel/LiveData.
- Glide for image loading.
- Firebase Authentication and Realtime Database SDK dependencies are present for future work.
- `google-services.json` is intentionally absent.
- The Google Services Gradle plugin is applied to `app` only when `app/google-services.json` exists, so the initial project builds without Firebase credentials.

## 4. Package and file architecture

Production Java code lives below `app/src/main/java/com/groupproject/justeat/`:

```text
base/
  BaseActivity.java
  BaseDialog.java
  BaseViewModel.java
data/
  model/
    Booking.java
    MenuCategory.java
    MenuItem.java
    Order.java
    OrderItem.java
    RestaurantTable.java
    User.java
  repository/
    AuthRepository.java
    BookingRepository.java
    MenuRepository.java
    OrderRepository.java
    RepositoryCallback.java
    TableRepository.java
domain/
  constant/
    BookingStatus.java
    OrderItemStatus.java
    OrderStatus.java
    TableStatus.java
    UserRole.java
    UserStatus.java
feature/
  login/
    LoginActivity.java
    LoginUiState.java
    LoginValidator.java
    LoginViewModel.java
ui/
  dialog/
    MessageDialog.java
util/
  GlideImageLoader.java
```

Each file has one responsibility. Future role-specific screens can be added under `feature/customer`, `feature/staff`, and `feature/manager` without changing the base APIs.

## 5. Shared base components

### `BaseActivity<VB extends ViewBinding>`

- Extends `AppCompatActivity`.
- Requires subclasses to provide `inflateBinding(LayoutInflater)`.
- Creates the binding before `setContentView`.
- Exposes the non-null binding to subclasses through `getBinding()`.
- Clears the binding reference during `onDestroy` to prevent holding destroyed views.
- Does not own a ViewModel because each screen chooses its own ViewModel type and factory.

### `BaseViewModel`

- Extends AndroidX `ViewModel`.
- Owns read-only `LiveData<Boolean>` for loading state.
- Owns read-only `LiveData<String>` for a general user-facing error.
- Provides protected setters for subclasses.
- Never stores an Activity, Context, View, ViewBinding, or Dialog reference.

### `BaseDialog<VB extends ViewBinding>`

- Extends `DialogFragment`.
- Requires subclasses to provide `inflateBinding(LayoutInflater, ViewGroup)`.
- Creates and releases ViewBinding with the dialog view lifecycle.
- Sets a consistent match-parent width and wrap-content height after the dialog starts.
- Serves as the parent for `MessageDialog`.

### `MessageDialog`

- Displays a title, message, and one positive button.
- Receives content through a `newInstance` factory and fragment arguments, allowing Android to recreate it safely.
- Is used for successful local validation and the not-yet-implemented sign-up action.

## 6. Login feature

### Layout

`activity_login.xml` follows the proposal’s English login wireframe and contains:

- A brand/restaurant `ImageView`.
- “Welcome” heading.
- Email `TextInputLayout` and `TextInputEditText`.
- Password `TextInputLayout` and `TextInputEditText` with a password visibility toggle.
- “Log In” Material button.
- “Don’t have an account? Sign Up” text action.
- A small progress indicator bound to the loading state.

The screen is scroll-safe on small devices. All user-facing text is stored in `strings.xml`. Content descriptions and labels are supplied for accessibility.

### Image loading

`LoginActivity` uses `GlideImageLoader` to load a bundled restaurant drawable into the header image. The utility also exposes normal rectangular, circular avatar, and placeholder/error variants for future remote image URLs.

No remote URL is required for the initial login page, keeping the screen deterministic and usable offline.

### Validation rules

`LoginValidator` is a pure Java class with no Activity dependency:

- Email is trimmed before validation.
- Empty email produces an email-required error.
- A non-empty malformed email produces an invalid-email error.
- Empty password produces a password-required error.
- Password shorter than six characters produces a minimum-length error.
- Valid input produces a success result.

### UI state and data flow

`LoginUiState` is an immutable state value containing loading state, email error, password error, general message, and validation-success state.

On the Log In button click:

1. `LoginActivity` reads email and password through ViewBinding.
2. It calls `LoginViewModel.validateLogin(email, password)`.
3. The ViewModel delegates validation to `LoginValidator`.
4. The ViewModel publishes one `LoginUiState` through LiveData.
5. The Activity clears old field errors, displays current field errors, updates button/progress visibility, or shows `MessageDialog` when validation succeeds.

No Firebase call and no navigation occur. The Sign Up action shows a message that the page will be added later.

## 7. Domain entities

All entity classes are Firebase-friendly Java beans. Every entity includes a public no-argument constructor, a public constructor containing every field, getters and setters for every field, and Chinese comments for the class, fields, constructors, and non-obvious constraints.

### `User`

- `String userId`
- `String firstName`
- `String lastName`
- `String email`
- `String phone`
- `String avatarUrl`
- `String role`: value from `UserRole`
- `String status`: value from `UserStatus`
- `long createdAt`
- `long updatedAt`

### `MenuCategory`

- `String categoryId`
- `String name`
- `String description`
- `int displayOrder`
- `boolean active`
- `long createdAt`
- `long updatedAt`

### `MenuItem`

- `String menuItemId`
- `String categoryId`
- `String name`
- `String description`
- `String imageUrl`
- `long priceInCents`: integer minor units avoid floating-point money errors
- `int spicinessLevel`: zero to five
- `boolean available`
- `long createdAt`
- `long updatedAt`

### `RestaurantTable`

- `String tableId`
- `String tableNumber`
- `int capacity`
- `String location`
- `String status`: value from `TableStatus`
- `long createdAt`
- `long updatedAt`

### `Booking`

- `String bookingId`
- `String customerId`
- `String tableId`
- `int guestCount`
- `long bookingTimeMillis`
- `String notes`
- `String status`: value from `BookingStatus`
- `long createdAt`
- `long updatedAt`

### `Order`

- `String orderId`
- `String customerId`
- `String bookingId`
- `String tableId`
- `long totalInCents`
- `String specialInstructions`
- `String status`: value from `OrderStatus`
- `long createdAt`
- `long updatedAt`

### `OrderItem`

- `String orderItemId`
- `String orderId`
- `String menuItemId`
- `String menuItemName`: name snapshot retained if the menu item changes later
- `long unitPriceInCents`: price snapshot retained at order time
- `int quantity`
- `long subtotalInCents`
- `String notes`
- `String status`: value from `OrderItemStatus`
- `long createdAt`
- `long updatedAt`

## 8. Status constants

Status values are string constants rather than Java enums so Firebase deserialization remains simple and backward-compatible.

- `UserRole`: `CUSTOMER`, `STAFF`, `MANAGER`
- `UserStatus`: `ACTIVE`, `SUSPENDED`, `DISABLED`
- `TableStatus`: `AVAILABLE`, `OCCUPIED`, `RESERVED`, `UNAVAILABLE`
- `BookingStatus`: `PENDING`, `CONFIRMED`, `REJECTED`, `CANCELLED`, `COMPLETED`
- `OrderStatus`: `PENDING`, `ACCEPTED`, `REJECTED`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED`
- `OrderItemStatus`: `PENDING`, `PREPARING`, `SERVED`, `CANCELLED`

Each constants class is final, has a private constructor, and documents where its values are stored.

## 9. Repository contracts

`RepositoryCallback<T>` provides `onSuccess(T data)` and `onError(String message)` for future asynchronous Firebase work.

The initial repository interfaces define future boundaries without implementations:

- `AuthRepository`: sign in, sign out, obtain the current user ID, and check signed-in state.
- `MenuRepository`: load all menu items, save a menu item, and delete a menu item.
- `TableRepository`: load all tables, save a table, and delete a table.
- `BookingRepository`: load bookings for a customer, save a booking, update its status, and cancel it.
- `OrderRepository`: load orders for a customer, load orders by status, save an order, and update its status.

Repository methods accept `RepositoryCallback` where results are asynchronous. They do not expose Firebase SDK types to Activity, ViewModel, or model packages.

## 10. Error handling and security boundaries

- Field validation errors remain next to their fields.
- General errors are exposed through the base ViewModel and displayed through a dialog or future Snackbar.
- The login button is disabled while loading to prevent duplicate submissions.
- Passwords are never written to logs, model entities, saved instance state, or Firebase Realtime Database.
- Firebase credentials are not committed. `.gitignore` excludes `google-services.json`, local SDK paths, Gradle state, IDE workspace state, and build outputs.
- Repository interfaces return user-facing error messages without leaking Firebase exception objects into UI code.

## 11. Testing and verification

JVM unit tests cover:

- Empty email.
- Invalid email format.
- Empty password.
- Password shorter than six characters.
- Valid credentials.
- Login ViewModel publishing field errors and success state.
- Entity no-argument construction, full construction, and representative getter/setter round trips.

Build verification commands:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The project is complete for this scope only when both commands exit successfully without requiring `google-services.json`.

## 12. Explicitly out of scope

- Real Firebase sign-in, account creation, sign-out, or database CRUD.
- `google-services.json` or any Firebase project credentials.
- Customer, staff, or manager screens.
- Navigation after successful validation.
- Registration, password reset, session persistence, dependency injection, Room, Retrofit, and Compose.
- Production repository implementations.

## 13. Acceptance criteria

- Android Studio opens and synchronizes the project at the requested path.
- All production screen code is Java and all UI layouts are XML.
- ViewBinding is enabled and used; `findViewById` is not used.
- `LoginActivity` launches first and performs only local validation.
- Glide is declared, wrapped, and exercised by the login screen.
- `BaseActivity`, `BaseViewModel`, `BaseDialog`, and `MessageDialog` compile and demonstrate their intended lifecycle behavior.
- Every approved entity, constant class, and repository contract exists with complete fields and comments.
- The project builds and unit tests pass without Firebase credentials.
