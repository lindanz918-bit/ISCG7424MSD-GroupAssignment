# Just Eat Android Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Java/XML Android foundation for Just Eat with one locally validated login screen, shared base classes, Glide, Firebase-ready contracts, and complete documented entities.

**Architecture:** Use one `app` module and a lightweight MVVM split. Android UI classes stay under `feature` and `ui`, reusable lifecycle plumbing stays under `base`, pure domain data stays under `data/model`, and Firebase-facing boundaries stay behind repository interfaces.

**Tech Stack:** Android Gradle Plugin 8.9.3, Gradle 8.11.1, Java 17, Android SDK 35, XML, ViewBinding, AppCompat, Material Components, AndroidX Lifecycle, Glide 4.16.0, Firebase BoM 34.1.0, JUnit 4, AndroidX Arch Core Testing, Robolectric.

**Spec:** `docs/superpowers/specs/2026-09-05-just-eat-android-foundation-design.md`

## Global Constraints

- Project path: `/Users/karson/AndroidStudioProjects/JustEat`.
- Application ID and namespace: `com.groupproject.justeat`.
- Production screen code is Java; layouts are XML.
- Minimum SDK 24; compile SDK and target SDK 35.
- Use ViewBinding everywhere in Activity and Dialog code; do not use `findViewById` in production.
- Implement only `LoginActivity`; do not create customer, staff, manager, registration, or home screens.
- Login performs local input validation only; it must not call Firebase or navigate.
- Declare Firebase Authentication and Realtime Database dependencies, but do not create `google-services.json` or a production Firebase repository.
- All entities require a public no-argument constructor, a constructor containing every field, getters/setters for every field, and Chinese comments.
- Never log or persist passwords.
- Use integer cents for money and `long` Unix epoch milliseconds for dates.
- Write behavior tests before production Java code and observe each intended failure.
- Gradle files, manifests, generated wrapper files, drawables, and XML layouts are configuration/assets; verify them through sync, Robolectric, and `assembleDebug` rather than standalone unit tests.

---

## File map

Project/configuration:

- `settings.gradle.kts`: repositories and `:app` inclusion.
- `build.gradle.kts`: Android and optional Google Services plugin declarations.
- `gradle/libs.versions.toml`: centralized dependency versions.
- `gradle.properties`: AndroidX and Gradle JVM flags.
- `gradle/wrapper/*`, `gradlew`, `gradlew.bat`: Gradle 8.11.1 wrapper.
- `.gitignore`: credentials, build outputs, IDE state, and local SDK exclusions.
- `app/build.gradle.kts`: Android configuration, ViewBinding, dependencies, and conditional Google Services application.
- `app/src/main/AndroidManifest.xml`: launcher Activity and Internet permission.

Production Java:

- `base/BaseActivity.java`: Activity ViewBinding lifecycle.
- `base/BaseViewModel.java`: shared loading and error LiveData.
- `base/BaseDialog.java`: DialogFragment ViewBinding lifecycle and size.
- `feature/login/LoginActivity.java`: input/output wiring only.
- `feature/login/LoginUiState.java`: immutable login rendering state.
- `feature/login/LoginValidator.java`: pure Java validation.
- `feature/login/LoginViewModel.java`: state orchestration.
- `ui/dialog/MessageDialog.java`: one-button informational dialog.
- `util/GlideImageLoader.java`: rectangular, placeholder, and circular Glide requests.
- `data/model/*.java`: seven Firebase-compatible Java beans.
- `domain/constant/*.java`: six Firebase-safe string constant groups.
- `data/repository/*.java`: callback plus five repository boundaries.

Resources:

- `layout/activity_login.xml`: scroll-safe login form.
- `layout/dialog_message.xml`: reusable message dialog.
- `drawable/ic_restaurant_brand.xml`: bundled restaurant illustration.
- `drawable/ic_image_placeholder.xml`: fallback image.
- `values/colors.xml`, `values/strings.xml`, `values/themes.xml`: centralized UI values.

Tests:

- `base/BaseViewModelTest.java`: loading/error behavior.
- `data/model/EntityModelTest.java`: constructors and property round trips.
- `data/repository/RepositoryContractTest.java`: public boundary signatures.
- `domain/constant/StatusConstantsTest.java`: stored Firebase values.
- `feature/login/LoginValidatorTest.java`: validation rules.
- `feature/login/LoginViewModelTest.java`: loading and state publication.
- `feature/login/LoginActivityTest.java`: Robolectric ViewBinding/UI integration.

---

### Task 1: Create a buildable empty Android project

**Files:**

- Create: `.gitignore`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create locally but do not commit: `local.properties`
- Create: `gradle/libs.versions.toml`
- Create: `app/.gitignore`
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Generate: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`

**Interfaces:**

- Produces: a single `:app` Android module with `BuildConfig.APPLICATION_ID == "com.groupproject.justeat"` and generated ViewBinding support.

- [ ] **Step 1: Add Gradle settings and version catalog**

Use this repository configuration:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "JustEat"
include(":app")
```

Use these exact catalog versions and aliases:

```toml
[versions]
agp = "8.9.3"
googleServices = "4.4.4"
appcompat = "1.7.1"
material = "1.12.0"
constraintlayout = "2.2.1"
lifecycle = "2.9.2"
glide = "4.16.0"
firebaseBom = "34.1.0"
junit = "4.13.2"
archCore = "2.2.0"
robolectric = "4.14.1"

[libraries]
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "appcompat" }
androidx-constraintlayout = { module = "androidx.constraintlayout:constraintlayout", version.ref = "constraintlayout" }
androidx-lifecycle-livedata = { module = "androidx.lifecycle:lifecycle-livedata", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel = { module = "androidx.lifecycle:lifecycle-viewmodel", version.ref = "lifecycle" }
material = { module = "com.google.android.material:material", version.ref = "material" }
glide = { module = "com.github.bumptech.glide:glide", version.ref = "glide" }
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { module = "com.google.firebase:firebase-auth" }
firebase-database = { module = "com.google.firebase:firebase-database" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-arch-core-testing = { module = "androidx.arch.core:core-testing", version.ref = "archCore" }
robolectric = { module = "org.robolectric:robolectric", version.ref = "robolectric" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

- [ ] **Step 2: Configure root and app builds**

Use the root plugin declarations:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
}
```

Configure `app/build.gradle.kts` with `namespace` and `applicationId` set to `com.groupproject.justeat`, SDK values `24/35/35`, Java 17, ViewBinding enabled, release minification disabled, and these dependencies:

```kotlin
plugins {
    alias(libs.plugins.android.application)
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.groupproject.justeat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.groupproject.justeat"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.material)
    implementation(libs.glide)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.robolectric)
}
```

- [ ] **Step 3: Add manifest, base values, and ignore rules**

Declare `android.permission.INTERNET`, `LoginActivity` as the exported launcher, `@style/Theme.JustEat`, `@string/app_name`, and `android:allowBackup="false"`. Add `google-services.json`, `local.properties`, `.gradle/`, `.idea/workspace.xml`, `build/`, and `app/build/` to `.gitignore`.

Use these exact project properties:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
```

Create untracked `local.properties` for this machine:

```properties
sdk.dir=/Users/karson/Library/Android/sdk
```

- [ ] **Step 4: Generate the Gradle 8.11.1 wrapper**

Run:

```bash
/Users/karson/AndroidStudioProjects/Moni\ KDS/gradlew -p /Users/karson/AndroidStudioProjects/JustEat wrapper --gradle-version 8.11.1 --distribution-type bin
```

Expected: wrapper scripts, properties, and JAR are generated under `JustEat`.

- [ ] **Step 5: Verify the empty module configuration**

Run:

```bash
./gradlew tasks
```

Expected: exit code 0 and Android build tasks are listed without requiring `google-services.json`.

- [ ] **Step 6: Commit**

```bash
git add .gitignore settings.gradle.kts build.gradle.kts gradle.properties gradle app
git commit -m "build: scaffold Just Eat Android project"
```

---

### Task 2: Add the shared BaseViewModel state contract

**Files:**

- Test: `app/src/test/java/com/groupproject/justeat/base/BaseViewModelTest.java`
- Create: `app/src/main/java/com/groupproject/justeat/base/BaseViewModel.java`

**Interfaces:**

- Produces: `LiveData<Boolean> getLoading()`, `LiveData<String> getErrorMessage()`, protected `setLoading(boolean)`, and protected `setErrorMessage(String)`.

- [ ] **Step 1: Write the failing BaseViewModel test**

```java
public class BaseViewModelTest {
    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static final class TestViewModel extends BaseViewModel {
        void updateLoading(boolean value) { setLoading(value); }
        void updateError(String value) { setErrorMessage(value); }
    }

    @Test public void startsIdleAndWithoutError() {
        TestViewModel viewModel = new TestViewModel();
        assertEquals(Boolean.FALSE, viewModel.getLoading().getValue());
        assertNull(viewModel.getErrorMessage().getValue());
    }

    @Test public void protectedSettersPublishState() {
        TestViewModel viewModel = new TestViewModel();
        viewModel.updateLoading(true);
        viewModel.updateError("Network unavailable");
        assertEquals(Boolean.TRUE, viewModel.getLoading().getValue());
        assertEquals("Network unavailable", viewModel.getErrorMessage().getValue());
    }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```bash
./gradlew testDebugUnitTest --tests '*.BaseViewModelTest'
```

Expected: compilation fails because `BaseViewModel` does not exist.

- [ ] **Step 3: Implement BaseViewModel**

```java
public abstract class BaseViewModel extends ViewModel {
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    protected void setLoading(boolean value) { loading.setValue(value); }
    protected void setErrorMessage(String value) { errorMessage.setValue(value); }
}
```

Add Chinese class, field, getter, and protected-setter comments without changing the API.

- [ ] **Step 4: Run the test and verify GREEN**

```bash
./gradlew testDebugUnitTest --tests '*.BaseViewModelTest'
```

Expected: two tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/groupproject/justeat/base/BaseViewModel.java app/src/test/java/com/groupproject/justeat/base/BaseViewModelTest.java
git commit -m "feat: add base ViewModel state"
```

---

### Task 3: Add Firebase-safe status constants and domain entities

**Files:**

- Test: `app/src/test/java/com/groupproject/justeat/domain/constant/StatusConstantsTest.java`
- Test: `app/src/test/java/com/groupproject/justeat/data/model/EntityModelTest.java`
- Create: `app/src/main/java/com/groupproject/justeat/domain/constant/UserRole.java`
- Create: `app/src/main/java/com/groupproject/justeat/domain/constant/UserStatus.java`
- Create: `app/src/main/java/com/groupproject/justeat/domain/constant/TableStatus.java`
- Create: `app/src/main/java/com/groupproject/justeat/domain/constant/BookingStatus.java`
- Create: `app/src/main/java/com/groupproject/justeat/domain/constant/OrderStatus.java`
- Create: `app/src/main/java/com/groupproject/justeat/domain/constant/OrderItemStatus.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/model/User.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/model/MenuCategory.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/model/MenuItem.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/model/RestaurantTable.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/model/Booking.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/model/Order.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/model/OrderItem.java`

**Interfaces:**

- Produces: Java beans with exact fields and constructors from the approved design spec.
- Produces: string constants whose values are persisted unchanged in Firebase.

- [ ] **Step 1: Write failing constant-value tests**

Assert the exact arrays below:

```java
assertArrayEquals(new String[]{"CUSTOMER", "STAFF", "MANAGER"},
        new String[]{UserRole.CUSTOMER, UserRole.STAFF, UserRole.MANAGER}
);
assertArrayEquals(new String[]{"ACTIVE", "SUSPENDED", "DISABLED"},
        new String[]{UserStatus.ACTIVE, UserStatus.SUSPENDED, UserStatus.DISABLED}
);
assertArrayEquals(new String[]{"AVAILABLE", "OCCUPIED", "RESERVED", "UNAVAILABLE"},
        new String[]{TableStatus.AVAILABLE, TableStatus.OCCUPIED,
                TableStatus.RESERVED, TableStatus.UNAVAILABLE}
);
assertArrayEquals(new String[]{"PENDING", "CONFIRMED", "REJECTED", "CANCELLED", "COMPLETED"},
        new String[]{BookingStatus.PENDING, BookingStatus.CONFIRMED,
                BookingStatus.REJECTED, BookingStatus.CANCELLED, BookingStatus.COMPLETED}
);
assertArrayEquals(new String[]{"PENDING", "ACCEPTED", "REJECTED", "PREPARING", "READY", "COMPLETED", "CANCELLED"},
        new String[]{OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.REJECTED,
                OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED,
                OrderStatus.CANCELLED}
);
assertArrayEquals(new String[]{"PENDING", "PREPARING", "SERVED", "CANCELLED"},
        new String[]{OrderItemStatus.PENDING, OrderItemStatus.PREPARING,
                OrderItemStatus.SERVED, OrderItemStatus.CANCELLED}
);
```

- [ ] **Step 2: Write failing entity-constructor and mutation tests**

For each entity, instantiate the no-argument constructor and assert it is non-null. Then instantiate the all-arguments constructor using these exact signatures and assert every getter equals its input:

```java
new User(String userId, String firstName, String lastName, String email,
        String phone, String avatarUrl, String role, String status,
        long createdAt, long updatedAt)

new MenuCategory(String categoryId, String name, String description,
        int displayOrder, boolean active, long createdAt, long updatedAt)

new MenuItem(String menuItemId, String categoryId, String name,
        String description, String imageUrl, long priceInCents,
        int spicinessLevel, boolean available, long createdAt, long updatedAt)

new RestaurantTable(String tableId, String tableNumber, int capacity,
        String location, String status, long createdAt, long updatedAt)

new Booking(String bookingId, String customerId, String tableId,
        int guestCount, long bookingTimeMillis, String notes, String status,
        long createdAt, long updatedAt)

new Order(String orderId, String customerId, String bookingId, String tableId,
        long totalInCents, String specialInstructions, String status,
        long createdAt, long updatedAt)

new OrderItem(String orderItemId, String orderId, String menuItemId,
        String menuItemName, long unitPriceInCents, int quantity,
        long subtotalInCents, String notes, String status,
        long createdAt, long updatedAt)
```

Call every setter once with a second value and assert its matching getter returns that value. Use `assertEquals(long, long)` for money/timestamps, `assertEquals(int, int)` for counts/order/spiciness, and `assertEquals(boolean, boolean)` for flags.

- [ ] **Step 3: Run both tests and verify RED**

```bash
./gradlew testDebugUnitTest --tests '*.StatusConstantsTest' --tests '*.EntityModelTest'
```

Expected: test compilation fails because the constants and entities do not exist.

- [ ] **Step 4: Implement constants**

Each constant group follows this complete shape with its approved values:

```java
public final class UserRole {
    public static final String CUSTOMER = "CUSTOMER";
    public static final String STAFF = "STAFF";
    public static final String MANAGER = "MANAGER";

    private UserRole() {
        throw new AssertionError("No instances");
    }
}
```

Implement the other five final classes with every value asserted in Step 1. Add Chinese class and value comments.

- [ ] **Step 5: Implement all seven entities**

For every constructor signature in Step 2, create private fields with identical names and types, an empty public constructor, the exact full constructor, and one public getter and setter per field. Example naming is exact: `getUserId()/setUserId`, `isActive()/setActive`, `isAvailable()/setAvailable`, `getPriceInCents()/setPriceInCents`, and `getBookingTimeMillis()/setBookingTimeMillis`.

Add Chinese comments covering class purpose, every field’s storage meaning/unit, Firebase’s no-argument-constructor requirement, and the full constructor.

- [ ] **Step 6: Run both tests and verify GREEN**

```bash
./gradlew testDebugUnitTest --tests '*.StatusConstantsTest' --tests '*.EntityModelTest'
```

Expected: all constant and entity tests pass.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/groupproject/justeat/domain app/src/main/java/com/groupproject/justeat/data/model app/src/test/java/com/groupproject/justeat/domain app/src/test/java/com/groupproject/justeat/data/model
git commit -m "feat: add documented Just Eat domain models"
```

---

### Task 4: Define repository boundaries without Firebase implementations

**Files:**

- Test: `app/src/test/java/com/groupproject/justeat/data/repository/RepositoryContractTest.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/repository/RepositoryCallback.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/repository/AuthRepository.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/repository/MenuRepository.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/repository/TableRepository.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/repository/BookingRepository.java`
- Create: `app/src/main/java/com/groupproject/justeat/data/repository/OrderRepository.java`

**Interfaces:**

- Produces: SDK-independent asynchronous contracts with the exact signatures below.

- [ ] **Step 1: Write a failing reflection contract test**

Use `Class.getMethod` and assert these methods and return types exist:

```java
RepositoryCallback.class.getMethod("onSuccess", Object.class); // void
RepositoryCallback.class.getMethod("onError", String.class);   // void

AuthRepository.class.getMethod("signIn", String.class, String.class, RepositoryCallback.class); // void
AuthRepository.class.getMethod("signOut", RepositoryCallback.class); // void
AuthRepository.class.getMethod("getCurrentUserId"); // String
AuthRepository.class.getMethod("isSignedIn");       // boolean

MenuRepository.class.getMethod("getMenuItems", RepositoryCallback.class); // void
MenuRepository.class.getMethod("saveMenuItem", MenuItem.class, RepositoryCallback.class); // void
MenuRepository.class.getMethod("deleteMenuItem", String.class, RepositoryCallback.class); // void

TableRepository.class.getMethod("getTables", RepositoryCallback.class); // void
TableRepository.class.getMethod("saveTable", RestaurantTable.class, RepositoryCallback.class); // void
TableRepository.class.getMethod("deleteTable", String.class, RepositoryCallback.class); // void

BookingRepository.class.getMethod("getBookingsForCustomer", String.class, RepositoryCallback.class); // void
BookingRepository.class.getMethod("saveBooking", Booking.class, RepositoryCallback.class); // void
BookingRepository.class.getMethod("updateBookingStatus", String.class, String.class, RepositoryCallback.class); // void
BookingRepository.class.getMethod("cancelBooking", String.class, RepositoryCallback.class); // void

OrderRepository.class.getMethod("getOrdersForCustomer", String.class, RepositoryCallback.class); // void
OrderRepository.class.getMethod("getOrdersByStatus", String.class, RepositoryCallback.class); // void
OrderRepository.class.getMethod("saveOrder", Order.class, RepositoryCallback.class); // void
OrderRepository.class.getMethod("updateOrderStatus", String.class, String.class, RepositoryCallback.class); // void
```

Assert `getCurrentUserId` returns `String`, `isSignedIn` returns primitive `boolean`, and every other method returns primitive `void`.

- [ ] **Step 2: Run the contract test and verify RED**

```bash
./gradlew testDebugUnitTest --tests '*.RepositoryContractTest'
```

Expected: test compilation fails because repository types do not exist.

- [ ] **Step 3: Implement repository interfaces**

Use `RepositoryCallback<T>` with `void onSuccess(T data)` and `void onError(String message)`. Use `RepositoryCallback<User>` for sign-in, `RepositoryCallback<List<MenuItem>>`, `RepositoryCallback<List<RestaurantTable>>`, `RepositoryCallback<List<Booking>>`, and `RepositoryCallback<List<Order>>` for list loads. Mutations return the saved entity or `Void` for delete/status/sign-out operations.

Add Chinese Javadoc describing every parameter and callback. Import only project model types and `java.util.List`; do not import any Firebase SDK type.

- [ ] **Step 4: Run the contract test and verify GREEN**

```bash
./gradlew testDebugUnitTest --tests '*.RepositoryContractTest'
```

Expected: all signature assertions pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/groupproject/justeat/data/repository app/src/test/java/com/groupproject/justeat/data/repository
git commit -m "feat: define Firebase-ready repository contracts"
```

---

### Task 5: Implement pure login validation and immutable UI state

**Files:**

- Test: `app/src/test/java/com/groupproject/justeat/feature/login/LoginValidatorTest.java`
- Create: `app/src/main/java/com/groupproject/justeat/feature/login/LoginUiState.java`
- Create: `app/src/main/java/com/groupproject/justeat/feature/login/LoginValidator.java`

**Interfaces:**

- Produces: `LoginUiState LoginValidator.validate(String email, String password)`.
- Produces: immutable `LoginUiState(boolean loading, int emailErrorResId, int passwordErrorResId, int messageResId, boolean validationSuccessful)` plus getters and factories. Resource ID `0` means no message.

- [ ] **Step 1: Write failing validator tests**

```java
@Test public void emptyEmailReturnsRequiredError() {
    LoginUiState state = validator.validate("   ", "123456");
    assertEquals(R.string.error_email_required, state.getEmailErrorResId());
    assertFalse(state.isValidationSuccessful());
}

@Test public void malformedEmailReturnsFormatError() {
    LoginUiState state = validator.validate("not-an-email", "123456");
    assertEquals(R.string.error_email_invalid, state.getEmailErrorResId());
}

@Test public void emptyPasswordReturnsRequiredError() {
    LoginUiState state = validator.validate("customer@example.com", " ");
    assertEquals(R.string.error_password_required, state.getPasswordErrorResId());
}

@Test public void shortPasswordReturnsLengthError() {
    LoginUiState state = validator.validate("customer@example.com", "12345");
    assertEquals(R.string.error_password_too_short, state.getPasswordErrorResId());
}

@Test public void validCredentialsReturnSuccess() {
    LoginUiState state = validator.validate(" customer@example.com ", "123456");
    assertEquals(0, state.getEmailErrorResId());
    assertEquals(0, state.getPasswordErrorResId());
    assertTrue(state.isValidationSuccessful());
    assertEquals(R.string.validation_success_message, state.getMessageResId());
}
```

- [ ] **Step 2: Run the validator test and verify RED**

```bash
./gradlew testDebugUnitTest --tests '*.LoginValidatorTest'
```

Expected: compilation fails because the validator and UI state do not exist.

- [ ] **Step 3: Implement immutable LoginUiState**

Provide final fields, a full public constructor, getters, and these factories:

```java
public static LoginUiState idle() {
    return new LoginUiState(false, 0, 0, 0, false);
}

public static LoginUiState loading() {
    return new LoginUiState(true, 0, 0, 0, false);
}

public static LoginUiState validationError(int emailErrorResId, int passwordErrorResId) {
    return new LoginUiState(false, emailErrorResId, passwordErrorResId, 0, false);
}

public static LoginUiState success(int messageResId) {
    return new LoginUiState(false, 0, 0, messageResId, true);
}
```

- [ ] **Step 4: Implement LoginValidator**

Return `R.string.error_email_required`, `R.string.error_email_invalid`, `R.string.error_password_required`, `R.string.error_password_too_short`, and `R.string.validation_success_message` through the immutable UI state. Keep their actual English copy in `strings.xml`, not Java.

Treat null inputs safely. Trim email and password for emptiness checks. Match non-empty email against a compiled case-insensitive pattern equivalent to `^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$`. Return both field errors together when both inputs are invalid. Do not log or retain the password.

- [ ] **Step 5: Run the validator test and verify GREEN**

```bash
./gradlew testDebugUnitTest --tests '*.LoginValidatorTest'
```

Expected: five tests pass.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/groupproject/justeat/feature/login/LoginUiState.java app/src/main/java/com/groupproject/justeat/feature/login/LoginValidator.java app/src/test/java/com/groupproject/justeat/feature/login/LoginValidatorTest.java
git commit -m "feat: add local login validation"
```

---

### Task 6: Add LoginViewModel state orchestration

**Files:**

- Test: `app/src/test/java/com/groupproject/justeat/feature/login/LoginViewModelTest.java`
- Create: `app/src/main/java/com/groupproject/justeat/feature/login/LoginViewModel.java`

**Interfaces:**

- Consumes: `BaseViewModel`, `LoginValidator`, `LoginUiState`.
- Produces: `LiveData<LoginUiState> getUiState()` and `void validateLogin(String email, String password)`.

- [ ] **Step 1: Write failing ViewModel tests**

Use `InstantTaskExecutorRule`. Assert the initial state through its five getters. Attach a real observer to `getLoading()`, call `validateLogin`, and assert loading emits `false`, `true`, `false` in that order. For invalid credentials assert the published state contains both required resource IDs. For valid credentials assert `isValidationSuccessful()` and `R.string.validation_success_message` are set.

```java
@Test public void validationPublishesLoadingAndSuccess() {
    LoginViewModel viewModel = new LoginViewModel(new LoginValidator());
    List<Boolean> loadingValues = new ArrayList<>();
    Observer<Boolean> observer = loadingValues::add;
    viewModel.getLoading().observeForever(observer);

    viewModel.validateLogin("customer@example.com", "123456");

    assertEquals(Arrays.asList(false, true, false), loadingValues);
    assertTrue(viewModel.getUiState().getValue().isValidationSuccessful());
    viewModel.getLoading().removeObserver(observer);
}
```

- [ ] **Step 2: Run the ViewModel test and verify RED**

```bash
./gradlew testDebugUnitTest --tests '*.LoginViewModelTest'
```

Expected: test compilation fails because `LoginViewModel` does not exist.

- [ ] **Step 3: Implement LoginViewModel**

```java
public class LoginViewModel extends BaseViewModel {
    private final LoginValidator validator;
    private final MutableLiveData<LoginUiState> uiState =
            new MutableLiveData<>(LoginUiState.idle());

    public LoginViewModel() {
        this(new LoginValidator());
    }

    LoginViewModel(LoginValidator validator) {
        this.validator = validator;
    }

    public LiveData<LoginUiState> getUiState() {
        return uiState;
    }

    public void validateLogin(String email, String password) {
        setLoading(true);
        uiState.setValue(LoginUiState.loading());
        LoginUiState result = validator.validate(email, password);
        setLoading(false);
        uiState.setValue(result);
    }
}
```

Add Chinese comments and keep the injectable constructor package-private for the same-package test.

- [ ] **Step 4: Run the ViewModel test and verify GREEN**

```bash
./gradlew testDebugUnitTest --tests '*.LoginViewModelTest'
```

Expected: all ViewModel tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/groupproject/justeat/feature/login/LoginViewModel.java app/src/test/java/com/groupproject/justeat/feature/login/LoginViewModelTest.java
git commit -m "feat: add login ViewModel state flow"
```

---

### Task 7: Build the ViewBinding UI foundation and LoginActivity

**Files:**

- Test: `app/src/test/java/com/groupproject/justeat/feature/login/LoginActivityTest.java`
- Create: `app/src/main/java/com/groupproject/justeat/base/BaseActivity.java`
- Create: `app/src/main/java/com/groupproject/justeat/base/BaseDialog.java`
- Create: `app/src/main/java/com/groupproject/justeat/ui/dialog/MessageDialog.java`
- Create: `app/src/main/java/com/groupproject/justeat/util/GlideImageLoader.java`
- Create: `app/src/main/java/com/groupproject/justeat/feature/login/LoginActivity.java`
- Create: `app/src/main/res/layout/activity_login.xml`
- Create: `app/src/main/res/layout/dialog_message.xml`
- Create: `app/src/main/res/drawable/ic_restaurant_brand.xml`
- Create: `app/src/main/res/drawable/ic_image_placeholder.xml`
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values/themes.xml`

**Interfaces:**

- Consumes: `LoginViewModel`, `LoginUiState`, generated `ActivityLoginBinding`, generated `DialogMessageBinding`.
- Produces: launcher `LoginActivity`, reusable generic Activity/Dialog binding lifecycle, `MessageDialog.newInstance(String, String)`, and three Glide image methods.

- [ ] **Step 1: Add XML resources required to compile a UI test**

Create a vertically scrolling login layout with IDs `ivBrand`, `tilEmail`, `etEmail`, `tilPassword`, `etPassword`, `btnLogin`, `progressLogin`, and `tvSignUp`. Create a dialog layout with IDs `tvDialogTitle`, `tvDialogMessage`, and `btnDialogPositive`. Use Material controls, 48dp minimum interactive heights, labels/content descriptions from strings, a blue primary color, an orange accent, and a light background.

Define this exact user-facing copy in `strings.xml`:

```xml
<string name="app_name">Just Eat</string>
<string name="login_welcome">Welcome</string>
<string name="login_subtitle">Sign in to book a table and order your meal</string>
<string name="email_label">Email address</string>
<string name="password_label">Password</string>
<string name="login_action">Log In</string>
<string name="sign_up_prompt">Don’t have an account?</string>
<string name="sign_up_action">Sign Up</string>
<string name="brand_image_description">Just Eat restaurant logo</string>
<string name="error_email_required">Email is required</string>
<string name="error_email_invalid">Enter a valid email address</string>
<string name="error_password_required">Password is required</string>
<string name="error_password_too_short">Password must be at least 6 characters</string>
<string name="validation_complete_title">Validation complete</string>
<string name="validation_success_message">Input validated. Firebase login can be connected later.</string>
<string name="coming_soon_title">Coming soon</string>
<string name="coming_soon_message">The sign-up page has not been implemented yet.</string>
<string name="dialog_ok">OK</string>
```

- [ ] **Step 2: Write failing Robolectric integration tests**

```java
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class LoginActivityTest {
    @Test public void emptySubmitShowsBothRequiredErrors() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .setup().get();
        ViewGroup content = activity.findViewById(android.R.id.content);
        ActivityLoginBinding binding = ActivityLoginBinding.bind(content.getChildAt(0));

        binding.btnLogin.performClick();

        assertEquals(activity.getString(R.string.error_email_required),
                binding.tilEmail.getError().toString());
        assertEquals(activity.getString(R.string.error_password_required),
                binding.tilPassword.getError().toString());
    }

    @Test public void validSubmitShowsMessageDialog() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .setup().get();
        ViewGroup content = activity.findViewById(android.R.id.content);
        ActivityLoginBinding binding = ActivityLoginBinding.bind(content.getChildAt(0));
        binding.etEmail.setText("customer@example.com");
        binding.etPassword.setText("123456");

        binding.btnLogin.performClick();
        activity.getSupportFragmentManager().executePendingTransactions();

        assertTrue(activity.getSupportFragmentManager()
                .findFragmentByTag(MessageDialog.TAG) instanceof MessageDialog);
    }

    @Test public void signUpShowsNotImplementedDialog() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .setup().get();
        activity.findViewById(R.id.tvSignUp).performClick();
        activity.getSupportFragmentManager().executePendingTransactions();
        assertTrue(activity.getSupportFragmentManager()
                .findFragmentByTag(MessageDialog.TAG) instanceof MessageDialog);
    }
}
```

- [ ] **Step 3: Run the Activity test and verify RED**

```bash
./gradlew testDebugUnitTest --tests '*.LoginActivityTest'
```

Expected: compilation fails because Activity, base classes, dialog, and image loader do not exist.

- [ ] **Step 4: Implement BaseActivity**

```java
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    private VB binding;

    protected abstract VB inflateBinding(LayoutInflater inflater);

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    protected final VB getBinding() {
        if (binding == null) throw new IllegalStateException("Binding is unavailable");
        return binding;
    }

    @Override protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
```

- [ ] **Step 5: Implement BaseDialog and MessageDialog**

`BaseDialog<VB>` inflates in `onCreateView`, returns `binding.getRoot()`, clears binding in `onDestroyView`, and sets window width to `MATCH_PARENT` and height to `WRAP_CONTENT` in `onStart`.

`MessageDialog` extends it, defines public tag `TAG = "MessageDialog"`, stores `ARG_TITLE` and `ARG_MESSAGE` arguments, assigns both TextViews, and dismisses from the positive button. Its factory is:

```java
public static MessageDialog newInstance(String title, String message) {
    MessageDialog dialog = new MessageDialog();
    Bundle arguments = new Bundle();
    arguments.putString(ARG_TITLE, title);
    arguments.putString(ARG_MESSAGE, message);
    dialog.setArguments(arguments);
    return dialog;
}
```

- [ ] **Step 6: Implement GlideImageLoader**

Expose these exact static methods and make the utility non-instantiable:

```java
public static void load(ImageView view, Object source)
public static void load(ImageView view, Object source,
        @DrawableRes int placeholder, @DrawableRes int error)
public static void loadCircle(ImageView view, Object source,
        @DrawableRes int placeholder)
```

Use `Glide.with(view)` so request lifecycle and Context come from the target view. Use `.placeholder`, `.error`, and `.circleCrop` only in their respective overloads.

- [ ] **Step 7: Implement LoginActivity**

`LoginActivity` extends `BaseActivity<ActivityLoginBinding>`, returns `ActivityLoginBinding.inflate(inflater)`, creates its ViewModel with `new ViewModelProvider(this).get(LoginViewModel.class)`, loads `R.drawable.ic_restaurant_brand` through `GlideImageLoader`, observes loading and UI state, and registers button/text clicks.

Rendering rules are exact:

```java
private void renderState(LoginUiState state) {
    getBinding().tilEmail.setError(state.getEmailErrorResId() == 0
            ? null : getString(state.getEmailErrorResId()));
    getBinding().tilPassword.setError(state.getPasswordErrorResId() == 0
            ? null : getString(state.getPasswordErrorResId()));
    if (state.isValidationSuccessful() && state.getMessageResId() != 0) {
        MessageDialog.newInstance(
                getString(R.string.validation_complete_title),
                getString(state.getMessageResId())
        ).show(getSupportFragmentManager(), MessageDialog.TAG);
    }
}

private void renderLoading(Boolean loading) {
    boolean active = Boolean.TRUE.equals(loading);
    getBinding().progressLogin.setVisibility(active ? View.VISIBLE : View.GONE);
    getBinding().btnLogin.setEnabled(!active);
}
```

The login click passes only current text values to `validateLogin`. The sign-up click shows a `MessageDialog` using localized “Coming soon” title/message. Do not retain or log the password.

- [ ] **Step 8: Run the Activity test and verify GREEN**

```bash
./gradlew testDebugUnitTest --tests '*.LoginActivityTest'
```

Expected: all three Robolectric tests pass.

- [ ] **Step 9: Build the debug APK**

```bash
./gradlew assembleDebug
```

Expected: exit code 0; APK exists at `app/build/outputs/apk/debug/app-debug.apk`; no `google-services.json` is required.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/groupproject/justeat/base app/src/main/java/com/groupproject/justeat/feature/login app/src/main/java/com/groupproject/justeat/ui app/src/main/java/com/groupproject/justeat/util app/src/main/res app/src/test/java/com/groupproject/justeat/feature/login
git commit -m "feat: add ViewBinding login screen and UI foundation"
```

---

### Task 8: Add setup guidance and run final verification

**Files:**

- Create: `README.md`
- Create: `docs/FIREBASE_SETUP.md`
- Verify: entire project against the approved design spec.

**Interfaces:**

- Produces: student-facing instructions for opening the project, running tests, locating packages, and later adding Firebase safely.

- [ ] **Step 1: Write README**

Document the current scope, JDK 17 requirement, Android SDK 35 requirement, `./gradlew testDebugUnitTest`, `./gradlew assembleDebug`, package map, and the explicit fact that successful login means local validation only.

- [ ] **Step 2: Write Firebase setup guide**

Document this exact future sequence:

1. Create or select a Firebase project.
2. Register Android app `com.groupproject.justeat`.
3. Download `google-services.json` to `app/google-services.json`.
4. Confirm the conditional plugin applies automatically on the next Gradle sync.
5. Enable Email/Password in Firebase Authentication.
6. Create Realtime Database and define security rules before data is written.
7. Implement repository interfaces in a separate `data/repository/firebase` package.
8. Never commit `google-services.json` or place passwords in Realtime Database.

- [ ] **Step 3: Verify no forbidden or missing patterns**

Run:

```bash
rg -n "findViewById|FirebaseAuth|getInstance\(|Log\.[vdiew]\(.*password" app/src/main/java
```

Expected: no matches.

Run:

```bash
test ! -e app/google-services.json
test -f app/src/main/java/com/groupproject/justeat/base/BaseActivity.java
test -f app/src/main/java/com/groupproject/justeat/base/BaseViewModel.java
test -f app/src/main/java/com/groupproject/justeat/base/BaseDialog.java
test -f app/src/main/java/com/groupproject/justeat/feature/login/LoginActivity.java
```

Expected: every command exits 0.

- [ ] **Step 4: Run the complete unit test suite**

```bash
./gradlew clean testDebugUnitTest
```

Expected: all tests pass with zero failures.

- [ ] **Step 5: Run a clean debug build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL` and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 6: Inspect changed files and acceptance criteria**

```bash
git status --short
git diff --check
find app/src/main/java/com/groupproject/justeat -type f | sort
```

Compare the results line by line with Sections 4, 11, and 13 of the design spec. Fix any missing comment, field, file, test, or resource before proceeding.

- [ ] **Step 7: Commit documentation and final verification fixes**

```bash
git add README.md docs app
git commit -m "docs: add Just Eat setup guidance"
```
