# Kotlin Multiplatform Mobile Specification

## Atriz Mobile Apps (Fraccio + Admin)

**Version:** 1.0.0  
**Date:** March 2026  
**Target Platforms:** Android, iOS  
**Architecture:** Kotlin Multiplatform (KMP) with shared business logic

---

## 1. Project Overview

### 1.1 Goals

- Consolidate **Fraccio** (residential management) and **Atriz Admin** (tenant/product management) mobile apps into a single KMP repository
- Share business logic, data models, networking, and state management across Android and iOS
- Maintain platform-native UI using Jetpack Compose (Android) and SwiftUI (iOS)
- Connect to existing backend services (`fraccio-back`, `atriz-back`, `sso-back`)

### 1.2 Repository Structure

```
atriz-mobile/
├── shared/                          # KMP shared module
│   ├── src/
│   │   ├── commonMain/              # Shared Kotlin code
│   │   │   ├── domain/              # Business logic, use cases
│   │   │   ├── data/                # Repositories, data sources
│   │   │   ├── network/             # API clients, DTOs
│   │   │   └── util/                # Shared utilities
│   │   ├── androidMain/             # Android-specific implementations
│   │   └── iosMain/                 # iOS-specific implementations
│   └── build.gradle.kts
├── android/                         # Android application
│   ├── fraccio/                     # Fraccio Android app module
│   └── atriz/                       # Atriz Admin Android app module
├── ios/                             # iOS application
│   ├── fraccio/                     # Fraccio iOS target
│   └── atriz/                       # Atriz Admin iOS target
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 2. Authentication & SSO Integration

### 2.1 SSO Service

**Base URL:** `https://auth.atriz.com.mx` (production)

The apps use a **two-step authentication flow**:

1. **Authenticate** - Validate credentials, receive `authToken` and available tenants
2. **Establish Session** - Select tenant, receive `accessToken` and `refreshToken`

### 2.2 Auth Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/auth/authenticate` | POST | Step 1: Validate credentials |
| `/v1/auth/establish-session` | POST | Step 2: Select tenant, get tokens |
| `/v1/auth/refresh` | POST | Refresh access token |
| `/v1/auth/logout` | POST | Invalidate session |
| `/v1/auth/verify-email` | POST | Verify email with token |
| `/v1/auth/forgot-password` | POST | Request password reset |
| `/v1/auth/reset-password` | POST | Reset password with token |
| `/v1/users/me` | GET | Get current user profile |
| `/v1/users/me/profile` | PUT | Update profile |

### 2.3 Auth Data Models

```kotlin
// commonMain/domain/auth/models/

data class AuthenticateRequest(
    val email: String,
    val password: String,
    val productCode: String  // "fraccio" or "atriz"
)

data class AuthenticateResponse(
    val authToken: String,
    val autoSelect: Boolean,
    val tenantId: String?,
    val availableTenants: List<AvailableTenant>,
    val user: AuthUser
)

data class AvailableTenant(
    val tenantId: String,
    val tenantName: String,
    val tenantSlug: String,
    val accessibleProducts: List<String>
)

data class EstablishSessionRequest(
    val authToken: String,
    val tenantId: String
)

data class EstablishSessionResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: AuthUser,
    val tenant: Tenant
)

data class AuthUser(
    val id: String,
    val email: String,
    val emailVerified: Boolean,
    val name: String?,
    val avatarUrl: String?
)

data class Tenant(
    val id: String,
    val name: String,
    val slug: String,
    val status: TenantStatus
)

enum class TenantStatus {
    PENDING, ACTIVE, SUSPENDED, CANCELLED
}
```

### 2.4 Token Management

```kotlin
// commonMain/data/auth/

interface TokenStorage {
    suspend fun saveTokens(accessToken: String, refreshToken: String, expiresAt: Long)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun getExpiresAt(): Long?
    suspend fun clearTokens()
    suspend fun isTokenValid(): Boolean
}

// Platform-specific implementations:
// - Android: EncryptedSharedPreferences
// - iOS: Keychain
```

---

## 3. Fraccio App Specification

### 3.1 Product Code

```kotlin
const val FRACCIO_PRODUCT_CODE = "fraccio"
```

### 3.2 API Configuration

**Base URL:** `https://dev-api.fraccio.mx` (dev) / `https://api.fraccio.mx` (prod)

**Required Headers:**
- `Authorization: Bearer <accessToken>`
- `x-residential-id: <activeResidentialId>` (context header for multi-residential users)

### 3.3 Sync Endpoint

**Endpoint:** `GET /v1/sync/me`

Returns all initial app data after authentication:

```kotlin
data class SyncResponse(
    val user: SyncUser,
    val navigation: SyncNavigation,
    val config: SyncConfig,
    val notifications: SyncNotifications,
    val context: SyncContext,
    val onboarding: SyncOnboarding?,
    val userHouses: List<SyncUserHouse>
)

data class SyncUser(
    val profile: UserProfile,
    val preferences: UserPreferences,
    val permissions: List<String>,
    val roles: List<String>
)

data class UserProfile(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val fullName: String?,
    val avatarUrl: String?
)

data class UserPreferences(
    val theme: Theme,
    val language: String,
    val timezone: String,
    val notifications: NotificationPrefs
)

enum class Theme { DARK, LIGHT, SYSTEM }

data class NotificationPrefs(
    val email: Boolean,
    val push: Boolean
)

data class SyncNavigation(
    val menu: List<MenuItem>
)

data class MenuItem(
    val id: String,
    val label: String,
    val icon: String,
    val path: String,
    val permissions: List<String>?,
    val children: List<MenuItem>?
)

data class SyncConfig(
    val products: List<Product>,
    val featureFlags: Map<String, Boolean>
)

data class SyncNotifications(
    val token: String,
    val topic: String,
    val serverUrl: String
)

data class SyncContext(
    val residentials: List<ResidentialSummary>,
    val activeResidentialId: String?
)

data class ResidentialSummary(
    val id: String,
    val tenantId: String,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val status: ResidentialStatus,
    val createdAt: String,
    val updatedAt: String
)

enum class ResidentialStatus { ACTIVE, INACTIVE }

data class SyncOnboarding(
    val completed: Boolean,
    val currentStep: Int,
    val stepData: Map<String, Any>
)

data class SyncUserHouse(
    val houseId: String,
    val houseNumber: String,
    val streetName: String,
    val isPrimary: Boolean
)
```

### 3.4 Feature Modules

#### 3.4.1 Dashboard

**Permissions:** `resident.read` (resident view) or `admin.manage`/`admin.read` (admin view)

**Resident Dashboard:**
- Balance card showing current debt
- Quick payment upload
- Recent payment history

**Admin Dashboard:**
- Overview statistics
- Pending validations count
- Revenue metrics

#### 3.4.2 Houses (Casas)

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/houses` | GET | List all houses |
| `/v1/houses/:id` | GET | Get house detail with residents |
| `/v1/houses` | POST | Create house |
| `/v1/houses/:id` | PUT | Update house |
| `/v1/houses/:id/residents` | POST | Add resident to house |
| `/v1/houses/:id/residents/:userId` | DELETE | Remove resident |
| `/v1/houses/:id/residents/:userId/primary` | PATCH | Set primary resident |

**Data Models:**

```kotlin
data class House(
    val id: String,
    val streetId: String?,
    val houseStreet: String,
    val houseNumber: String,
    val propertyType: String,
    val status: HouseStatus,
    val floor: Int?,
    val fee: Double?,
    val residentCount: Int?,
    val residents: List<HouseResident>?,
    val streetName: String?,
    val typeStreet: String?
)

enum class HouseStatus { ACTIVE, INACTIVE }

data class HouseResident(
    val id: String,
    val userId: String,
    val name: String?,
    val fatherLastName: String?,
    val motherLastName: String?,
    val phone: String?,
    val relationshipType: RelationshipType,
    val isPrimary: Boolean
)

enum class RelationshipType { OWNER, TENANT, OTHER }

data class CreateHouseInput(
    val streetId: String,
    val houseNumber: String,
    val propertyType: String,
    val status: HouseStatus,
    val floor: Int?
)

data class UpdateHouseInput(
    val streetId: String?,
    val houseStreet: String?,
    val houseNumber: String?,
    val propertyType: String?,
    val status: HouseStatus?,
    val floor: Int?
)

data class AddResidentInput(
    val userId: String,
    val relationshipType: RelationshipType?,
    val isPrimary: Boolean?
)
```

#### 3.4.3 Streets (Calles)

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/streets` | GET | List all streets |
| `/v1/streets/active` | GET | List active streets only |
| `/v1/streets/:id` | GET | Get street detail with houses and stats |
| `/v1/streets` | POST | Create street |
| `/v1/streets/with-houses` | POST | Create street with bulk houses |
| `/v1/streets/:id` | PUT | Update street |

**Data Models:**

```kotlin
data class Street(
    val id: String,
    val streetName: String,
    val typeStreet: String,
    val status: StreetStatus,
    val houseCount: Int?,
    val residentCount: Int?,
    val createdAt: String?,
    val updatedAt: String?
)

enum class StreetStatus { ACTIVE, INACTIVE }

data class StreetHouse(
    val id: String,
    val houseNumber: String,
    val propertyType: String,
    val floor: Int?,
    val status: String,
    val residentCount: Int,
    val hasDebt: Boolean,
    val monthlyFee: Double?
)

data class StreetStats(
    val totalExpectedRevenue: Double,
    val delinquentHouseCount: Int,
    val unoccupiedHouseCount: Int
)

data class CreateStreetInput(
    val streetName: String,
    val typeStreet: String,
    val status: StreetStatus
)

data class CreateStreetWithHousesInput(
    val streetName: String,
    val typeStreet: String,
    val houses: List<BulkHouseInput>
)

data class BulkHouseInput(
    val houseNumber: String,
    val propertyType: String,
    val floor: Int?
)
```

#### 3.4.4 Charges (Cargos)

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/charges` | GET | List charges |
| `/v1/charges/:id` | GET | Get charge detail |
| `/v1/charges` | POST | Create charge |
| `/v1/charges/:id` | PUT | Update charge |
| `/v1/charges/house/:houseId` | GET | Get charges for house |
| `/v1/charges/house/:houseId/debt` | GET | Get debt summary |

**Data Models:**

```kotlin
data class Charge(
    val id: String,
    val houseId: String,
    val chargeType: ChargeType,
    val amount: Double,
    val description: String?,
    val status: ChargeStatus,
    val periodMonth: Int?,
    val periodYear: Int?,
    val dueDate: String?,
    val createdAt: String?,
    val createdBy: String?,
    val violationTypeId: String?,
    val useTypeId: String?,
    val updatedAt: String?
)

enum class ChargeType {
    MONTHLY_FEE,
    MONTHLY_FEE_OVERDUE,
    VIOLATIONS,
    USE,
    EXTERNAL,
    INSTALLMENT
}

enum class ChargeStatus {
    GENERATED,
    PENDING,
    PAID,
    OVERDUE,
    CANCELLED
}

data class CreateChargePayload(
    val houseId: String,
    val chargeType: ChargeType,
    val amount: Double,
    val description: String?,
    val periodMonth: Int?,
    val periodYear: Int?,
    val dueDate: String?,
    val violationTypeId: String?,
    val useTypeId: String?
)

data class DebtSummary(
    val currentDebt: Double
)
```

#### 3.4.5 Payments (Pagos)

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/payments/pending-validation` | GET | List pending payments (admin) |
| `/v1/payments/my-history` | GET | List user's payment history |
| `/v1/payments/houses/:houseId/history` | GET | List house payment history |
| `/v1/payments/:id/details` | GET | Get payment details |
| `/v1/payments/:id/validate` | PUT | Validate payment |
| `/v1/payments/:id/reject` | PUT | Reject payment |
| `/v1/payments/cash` | POST | Register cash payment |
| `/v1/payments/houses/:houseId/balance-in-favor` | GET | Get balance in favor |
| `/v1/payments/upload-receipt` | POST | Upload payment receipt (multipart) |

**Data Models:**

```kotlin
data class Payment(
    val id: String,
    val houseId: String,
    val userId: String?,
    val paymentType: PaymentType,
    val totalAmount: Double,
    val status: PaymentStatus,
    val submittedAt: String,
    val validatedAt: String?,
    val validatedBy: String?,
    val rejectionReason: String?,
    val notes: String?,
    val dateOfIncome: String?,
    val receiptUrl: String?,
    val balance: Double?,
    val createdAt: String
)

enum class PaymentType { SPEI, CASH, AUTOMATED }

enum class PaymentStatus {
    PENDING_VALIDATION,
    VALIDATED,
    REJECTED,
    PARCIAL
}

data class PaymentDetails(
    val payment: Payment,
    val chargeItems: List<PaymentChargeItem>,
    val house: PaymentHouse
)

data class PaymentChargeItem(
    val id: String,
    val chargeId: String,
    val amountApplied: Double,
    val charge: ChargeInfo
)

data class ChargeInfo(
    val id: String,
    val description: String?,
    val amount: Double,
    val chargeType: String,
    val dueDate: String?
)

data class PaymentHouse(
    val id: String,
    val houseNumber: String,
    val streetName: String
)

data class ValidatePaymentPayload(
    val chargeItems: List<ChargeItemInput>,
    val notes: String?,
    val dateOfIncome: String?
)

data class ChargeItemInput(
    val chargeId: String,
    val amountApplied: Double
)

data class RejectPaymentPayload(
    val rejectionReason: String
)

data class RegisterCashPaymentPayload(
    val houseId: String,
    val chargeItems: List<ChargeItemInput>,
    val totalAmount: Double,
    val notes: String?,
    val dateOfIncome: String?
)

data class BalanceInFavorResponse(
    val balance: Double
)
```

#### 3.4.6 Users & Invitations

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/users` | GET | List residential users |
| `/v1/users/:id` | GET | Get user detail |
| `/v1/invitations` | GET | List invitations |
| `/v1/invitations` | POST | Create invitation |
| `/v1/invitations/:id/resend` | POST | Resend invitation |
| `/v1/invitations/:id/cancel` | DELETE | Cancel invitation |
| `/v1/roles` | GET | List available roles |

**Data Models:**

```kotlin
data class ResidentialUser(
    val ssoUserId: String,
    val email: String,
    val name: String?,
    val fatherLastName: String?,
    val motherLastName: String?,
    val phone: String?,
    val roleName: String,
    val status: String,
    val houseId: String?,
    val houseNumber: String?,
    val houseStreet: String?
)

data class Invitation(
    val id: String,
    val email: String,
    val name: String?,
    val fatherLastName: String?,
    val motherLastName: String?,
    val phone: String?,
    val roleId: String,
    val roleName: String?,
    val houseId: String?,
    val houseName: String?,
    val status: InvitationStatus,
    val expiresAt: String,
    val createdAt: String,
    val acceptedAt: String?,
    val cancelledAt: String?,
    val resendCount: Int
)

enum class InvitationStatus {
    PENDING, ACCEPTED, EXPIRED, CANCELLED
}

data class Role(
    val id: String,
    val name: String,
    val description: String?,
    val isActive: Boolean
)

data class CreateInvitationInput(
    val email: String,
    val name: String?,
    val fatherLastName: String?,
    val motherLastName: String?,
    val phone: String?,
    val roleId: String,
    val houseId: String?,
    val metadata: Map<String, Any>?
)
```

#### 3.4.7 Residentials (Settings)

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/residentials` | GET | List residentials |
| `/v1/residentials/:id` | GET | Get residential detail |
| `/v1/residentials/:id` | PUT | Update residential |
| `/v1/residentials/:id/settings` | GET | Get settings |
| `/v1/residentials/:id/settings` | PUT | Update settings |
| `/v1/property-types/:residentialId` | GET | List property type fees |
| `/v1/property-types/:residentialId/active` | GET | List active fees |
| `/v1/property-types/catalog` | GET | Get property type catalog |
| `/v1/property-types/:residentialId` | POST | Create fee |
| `/v1/property-types/:residentialId/:feeId/fee` | PUT | Update fee |
| `/v1/property-types/:residentialId/:feeId` | DELETE | Delete fee |

**Data Models:**

```kotlin
data class Residential(
    val id: String,
    val name: String,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val country: String?,
    val status: ResidentialStatus,
    val createdAt: String?,
    val updatedAt: String?
)

data class ResidentialSettings(
    val id: String?,
    val residentialId: String,
    val chargeDay: Int,
    val overdueDay: Int,
    val createdAt: String?,
    val updatedAt: String?
)

data class PropertyTypeOption(
    val id: String?,
    val propertyType: String,
    val monthlyFeeAmount: Double,
    val monthlyFeeOverdue: Double?,
    val floor: Int?,
    val isActive: Boolean?
)
```

#### 3.4.8 Violations & Use Types

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/violation-types` | GET | List violation types |
| `/v1/violation-types` | POST | Create violation type |
| `/v1/violation-types/:id` | PUT | Update violation type |
| `/v1/use-types` | GET | List use types |
| `/v1/use-types` | POST | Create use type |
| `/v1/use-types/:id` | PUT | Update use type |

**Data Models:**

```kotlin
data class ViolationType(
    val id: String,
    val residentialId: String,
    val name: String,
    val description: String?,
    val defaultAmount: Double,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

data class UseType(
    val id: String,
    val residentialId: String,
    val name: String,
    val description: String?,
    val defaultAmount: Double,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)
```

#### 3.4.9 Onboarding

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/onboarding/status` | GET | Get onboarding status |
| `/v1/onboarding/progress` | POST | Save step progress |
| `/v1/onboarding/complete` | POST | Mark onboarding complete |

**Admin Onboarding Steps:**
1. **Fees** - Define property type fees
2. **Dates** - Configure charge day and overdue day
3. **Streets** - Create streets and houses structure
4. **Stripe** - Connect Stripe account for online payments

**Resident Onboarding Steps:**
1. **Invite Residents** - Invite household members

---

## 4. Atriz Admin App Specification

### 4.1 Product Code

```kotlin
const val ATRIZ_PRODUCT_CODE = "atriz"
```

### 4.2 API Configuration

**Base URL:** `https://dev-api.atriz.com.mx` (dev) / `https://api.atriz.com.mx` (prod)

**Required Headers:**
- `Authorization: Bearer <accessToken>`

### 4.3 Feature Modules

#### 4.3.1 Tenants

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/tenants` | GET | List tenants |
| `/v1/tenants/:id` | PUT | Update tenant |
| `/v1/tenants/:id/activate` | PUT | Activate tenant |
| `/v1/tenants/:id/suspend` | PUT | Suspend tenant |
| `/v1/tenants/:id/reactivate` | PUT | Reactivate tenant |
| `/v1/tenants/:id/cancel` | PUT | Cancel tenant |
| `/v1/tenants/:id/products` | GET | List tenant products |
| `/v1/tenants/:id/products` | POST | Assign product |
| `/v1/tenants/:id/products/:productId` | PUT | Update product assignment |
| `/v1/tenants/:id/products/:productId` | DELETE | Revoke product |
| `/v1/tenants/:id/users` | GET | List tenant users |
| `/v1/tenants/:id/users/sync` | POST | Sync users from SSO |
| `/v1/tenants/:id/invitations` | GET | List invitations |
| `/v1/tenants/:id/invitations` | POST | Create invitation |
| `/v1/tenants/:id/invitations/:invId` | DELETE | Cancel invitation |
| `/v1/tenants/:id/invitations/:invId/resend` | POST | Resend invitation |

**Data Models:**

```kotlin
data class Tenant(
    val id: String,
    val name: String,
    val slug: String,
    val status: TenantStatus,
    val billingEmail: String,
    val timezone: String,
    val currency: String,
    val billingStreet: String?,
    val billingColony: String?,
    val billingCity: String?,
    val billingState: String?,
    val billingPostalCode: String?,
    val billingCountry: String?,
    val billingRfc: String?,
    val billingLegalName: String?,
    val billingTaxRegime: String?,
    val billingCfdiUse: String?,
    val contractUrl: String?,
    val stripeCustomerId: String?,
    val termsAcceptedAt: String?,
    val termsAcceptedByEmail: String?,
    val onboardingCompleted: Boolean,
    val suspensionReason: String?,
    val suspendedAt: String?,
    val suspendedBy: String?,
    val cancelledAt: String?,
    val cancelledBy: String?,
    val createdBy: String?,
    val createdAt: String,
    val updatedAt: String
)

data class TenantProduct(
    val id: String,
    val tenantId: String,
    val productId: String,
    val status: TenantProductStatus,
    val billingModel: String?,
    val unitName: String?,
    val agreedUnitPrice: Double?,
    val agreedBasePrice: Double?,
    val currency: String?,
    val tierConfig: Map<String, Any>?,
    val defaultRole: String?,
    val activationDate: String?,
    val provisionedInProduct: Boolean,
    val provisionedAt: String?,
    val cancellationDate: String?,
    val cancellationReason: String?,
    val config: Map<String, Any>?,
    val createdAt: String,
    val updatedAt: String
)

enum class TenantProductStatus { ACTIVE, SUSPENDED, CANCELLED }

data class TenantUser(
    val id: String,
    val tenantId: String,
    val ssoUserId: String,
    val email: String,
    val name: String?,
    val isPrimary: Boolean,
    val isBlocked: Boolean,
    val lastSyncedAt: String?,
    val createdAt: String,
    val updatedAt: String
)

data class TenantInvitation(
    val id: String,
    val tenantId: String,
    val tenantName: String?,
    val email: String,
    val name: String?,
    val fatherLastName: String?,
    val motherLastName: String?,
    val phone: String?,
    val productCode: String?,
    val contractUrl: String?,
    val metadata: Map<String, Any>?,
    val invitedBy: String,
    val status: InvitationStatus,
    val expiresAt: String,
    val cancelledBy: String?,
    val cancelledAt: String?,
    val resendCount: Int,
    val lastResentAt: String?,
    val lastResentBy: String?,
    val acceptedAt: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateTenantInput(
    val name: String,
    val slug: String,
    val billingEmail: String,
    val timezone: String?,
    val currency: String?,
    val billingStreet: String?,
    val billingColony: String?,
    val billingCity: String?,
    val billingState: String?,
    val billingPostalCode: String?,
    val billingCountry: String?,
    val billingRfc: String?,
    val billingLegalName: String?,
    val billingTaxRegime: String?,
    val billingCfdiUse: String?,
    val contractUrl: String?
)

data class AssignProductInput(
    val productId: String,
    val billingModel: String?,
    val unitName: String?,
    val agreedUnitPrice: Double?,
    val agreedBasePrice: Double?,
    val currency: String?,
    val tierConfig: Map<String, Any>?,
    val defaultRole: String?
)
```

#### 4.3.2 Products

**Endpoints:**
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/products` | GET | List products |
| `/v1/products` | POST | Create product |
| `/v1/products/:id` | PUT | Update product |
| `/v1/products/:id/platforms` | GET | List platforms |
| `/v1/products/:id/platforms` | POST | Add platform |
| `/v1/products/:id/platforms/:platformId` | PUT | Update platform |
| `/v1/products/:id/features` | GET | List features |
| `/v1/products/:id/features` | POST | Add feature |
| `/v1/products/:id/features/:featureId` | PUT | Update feature |
| `/v1/products/:id/versions` | GET | List versions |
| `/v1/products/:id/versions` | POST | Add version |
| `/v1/products/:id/versions/:versionId` | PUT | Update version |

**Data Models:**

```kotlin
data class Product(
    val id: String,
    val code: String,
    val name: String,
    val description: String?,
    val iconUrl: String?,
    val baseUrl: String?,
    val internalApiUrl: String?,
    val defaultBillingModel: String?,
    val defaultUnitName: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class ProductPlatform(
    val id: String,
    val productId: String,
    val platform: String,
    val isAvailable: Boolean,
    val storeUrl: String?,
    val minVersion: String?,
    val latestVersion: String?,
    val releaseChannel: String?,
    val createdAt: String,
    val updatedAt: String
)

data class ProductFeature(
    val id: String,
    val productId: String,
    val featureKey: String,
    val featureName: String,
    val description: String?,
    val scope: String?,
    val owningSquad: String?,
    val rolloutStatus: String?,
    val rolloutNotes: String?,
    val flagsmithFlagId: String?,
    val platforms: List<String>?,
    val isBaseline: Boolean,
    val isEnabled: Boolean,
    val config: Map<String, Any>?,
    val createdAt: String,
    val updatedAt: String
)

data class ProductVersion(
    val id: String,
    val productId: String,
    val platform: String,
    val version: String,
    val releaseDate: String?,
    val adoptionPercentage: Double?,
    val isSupported: Boolean,
    val deprecationDate: String?,
    val createdAt: String,
    val updatedAt: String
)
```

---

## 5. Shared Architecture

### 5.1 Network Layer

```kotlin
// commonMain/network/

interface ApiClient {
    suspend fun <T> get(path: String, params: Map<String, String>? = null): ApiResponse<T>
    suspend fun <T> post(path: String, body: Any? = null): ApiResponse<T>
    suspend fun <T> put(path: String, body: Any? = null): ApiResponse<T>
    suspend fun <T> patch(path: String, body: Any? = null): ApiResponse<T>
    suspend fun <T> delete(path: String, body: Any? = null): ApiResponse<T>
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: ApiError?
)

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any>?
)

// Implementation using Ktor
class KtorApiClient(
    private val baseUrl: String,
    private val tokenStorage: TokenStorage,
    private val authRefresher: AuthRefresher
) : ApiClient {
    // Auto-inject Authorization header
    // Auto-refresh on 401
    // Retry logic
}
```

### 5.2 State Management

Use **Kotlin Multiplatform StateFlow** pattern:

```kotlin
// commonMain/util/

abstract class Store<State, Action> {
    protected abstract val initialState: State
    
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()
    
    protected fun setState(reducer: (State) -> State) {
        _state.update(reducer)
    }
    
    abstract suspend fun dispatch(action: Action)
}

// Example usage:
class HouseStore(private val repository: HouseRepository) : Store<HouseState, HouseAction>() {
    override val initialState = HouseState()
    
    override suspend fun dispatch(action: HouseAction) {
        when (action) {
            is HouseAction.LoadHouses -> loadHouses()
            is HouseAction.SelectHouse -> selectHouse(action.id)
            // ...
        }
    }
}
```

### 5.3 Repository Pattern

```kotlin
// commonMain/data/

interface HouseRepository {
    suspend fun getHouses(): Result<List<House>>
    suspend fun getHouseDetail(id: String): Result<HouseDetail>
    suspend fun createHouse(input: CreateHouseInput): Result<House>
    suspend fun updateHouse(id: String, input: UpdateHouseInput): Result<House>
    suspend fun addResident(houseId: String, input: AddResidentInput): Result<Unit>
    suspend fun removeResident(houseId: String, userId: String): Result<Unit>
}

class HouseRepositoryImpl(
    private val apiClient: ApiClient
) : HouseRepository {
    override suspend fun getHouses(): Result<List<House>> {
        return try {
            val response = apiClient.get<HousesResponse>("/v1/houses")
            if (response.success && response.data != null) {
                Result.success(response.data.houses)
            } else {
                Result.failure(ApiException(response.error?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ...
}
```

### 5.4 Dependency Injection

Use **Koin** for multiplatform DI:

```kotlin
// commonMain/di/

val sharedModule = module {
    // Network
    single<TokenStorage> { platformTokenStorage() }
    single<ApiClient> { KtorApiClient(get(), get(), get()) }
    
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<HouseRepository> { HouseRepositoryImpl(get()) }
    single<StreetRepository> { StreetRepositoryImpl(get()) }
    // ...
    
    // Stores
    factory { AuthStore(get()) }
    factory { HouseStore(get()) }
    // ...
}

// Platform-specific
expect fun platformTokenStorage(): TokenStorage
```

---

## 6. UI Specifications

### 6.1 Navigation Structure

**Fraccio App:**
```
├── Auth
│   ├── Sign In (two-step: credentials → tenant selection)
│   └── Invitation Accept
├── Onboarding (wizard flow)
└── Main (authenticated)
    ├── Dashboard
    ├── Finances
    │   ├── Charges
    │   ├── Payments (admin: validation, resident: history)
    │   └── My Payments
    ├── Community
    │   ├── Houses
    │   ├── Streets
    │   └── Users
    ├── Settings
    │   ├── Residentials
    │   ├── Violations
    │   └── Use Types
    └── Notifications
```

**Atriz Admin App:**
```
├── Auth
│   ├── Sign In
│   └── Invitation Accept
└── Main (authenticated)
    ├── Dashboard
    ├── Tenants
    │   ├── List
    │   ├── Detail
    │   ├── Create
    │   ├── Invitations
    │   └── Provision
    └── Products
        ├── List
        └── Detail (platforms, features, versions)
```

### 6.2 Design System

Both apps should follow the existing design patterns:

- **Theme:** Dark/Light/System modes
- **Colors:** Primary brand colors per app
- **Typography:** System fonts with consistent sizing
- **Components:**
  - Cards with rounded corners
  - Bottom sheets for actions
  - Pull-to-refresh on lists
  - Skeleton loading states
  - Toast notifications
  - Dialogs for confirmations
  - Form validation with inline errors

### 6.3 Platform-Specific UI

**Android (Jetpack Compose):**
- Material 3 design
- Navigation Compose
- Accompanist libraries for system UI

**iOS (SwiftUI):**
- Native iOS design patterns
- NavigationStack
- Native system components

---

## 7. Push Notifications

### 7.1 ntfy Integration

Both apps use **ntfy** for push notifications:

```kotlin
data class NotificationConfig(
    val serverUrl: String,  // From sync response
    val topic: String,      // From sync response
    val token: String       // Bearer token for auth
)
```

### 7.2 Notification Topics

- Per-user topics for personal notifications
- Per-residential topics for community announcements (Fraccio)
- Admin topics for system alerts (Atriz)

---

## 8. Offline Support

### 8.1 Caching Strategy

- **SQLDelight** for local database
- Cache sync response data
- Queue mutations when offline
- Sync on reconnection

### 8.2 Cached Entities

**Fraccio:**
- Houses, Streets, Charges (read-heavy)
- User profile and preferences
- Menu configuration

**Atriz:**
- Tenants list (with pagination)
- Products catalog

---

## 9. Security Requirements

### 9.1 Token Storage

- **Android:** EncryptedSharedPreferences
- **iOS:** Keychain with appropriate access control

### 9.2 Certificate Pinning

Implement certificate pinning for production builds:
- SSO service
- Product API services

### 9.3 Biometric Authentication

Optional biometric unlock for returning users:
- Fingerprint (Android)
- Face ID / Touch ID (iOS)

---

## 10. Build & Deployment

### 10.1 Build Variants

| Variant | SSO URL | API URL | Features |
|---------|---------|---------|----------|
| Debug | dev auth | dev API | Logging, debug tools |
| Staging | staging auth | staging API | Pre-release testing |
| Release | prod auth | prod API | Production |

### 10.2 CI/CD

- GitHub Actions for builds
- Fastlane for iOS deployment
- Gradle for Android deployment
- TestFlight / Play Store internal tracks

---

## 11. Dependencies

### 11.1 Shared (KMP)

```kotlin
// build.gradle.kts (shared module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Networking
            implementation("io.ktor:ktor-client-core:2.3.x")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.x")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.x")
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.x")
            
            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.x")
            
            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.x")
            
            // DI
            implementation("io.insert-koin:koin-core:3.5.x")
            
            // Database
            implementation("app.cash.sqldelight:runtime:2.0.x")
        }
        
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:2.3.x")
            implementation("app.cash.sqldelight:android-driver:2.0.x")
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
        }
        
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.x")
            implementation("app.cash.sqldelight:native-driver:2.0.x")
        }
    }
}
```

### 11.2 Android

```kotlin
// Compose
implementation("androidx.compose.ui:ui:1.6.x")
implementation("androidx.compose.material3:material3:1.2.x")
implementation("androidx.navigation:navigation-compose:2.7.x")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.x")

// Koin
implementation("io.insert-koin:koin-android:3.5.x")
implementation("io.insert-koin:koin-androidx-compose:3.5.x")

// Image loading
implementation("io.coil-kt:coil-compose:2.5.x")
```

### 11.3 iOS

```swift
// Swift Package Manager
.package(url: "https://github.com/Alamofire/Alamofire.git", from: "5.8.0"),
.package(url: "https://github.com/kean/Nuke.git", from: "12.0.0"),
```

---

## 12. Migration Checklist

### 12.1 Phase 1: Foundation
- [ ] Set up KMP project structure
- [ ] Implement shared networking layer
- [ ] Implement token storage (platform-specific)
- [ ] Implement auth flow (authenticate + establish session)
- [ ] Create base UI scaffolding for both platforms

### 12.2 Phase 2: Fraccio Core
- [ ] Implement sync endpoint integration
- [ ] Build dashboard (resident + admin views)
- [ ] Build houses feature (list, detail, CRUD)
- [ ] Build streets feature (list, detail, CRUD)
- [ ] Build charges feature

### 12.3 Phase 3: Fraccio Payments
- [ ] Build payment validation (admin)
- [ ] Build payment history (resident)
- [ ] Implement receipt upload
- [ ] Build cash payment registration

### 12.4 Phase 4: Fraccio Settings
- [ ] Build users/invitations management
- [ ] Build residential settings
- [ ] Build violation types
- [ ] Build use types
- [ ] Implement onboarding wizard

### 12.5 Phase 5: Atriz Admin
- [ ] Build tenants list and detail
- [ ] Build tenant CRUD operations
- [ ] Build product assignment flow
- [ ] Build products management
- [ ] Build invitations flow

### 12.6 Phase 6: Polish
- [ ] Implement push notifications
- [ ] Add offline support
- [ ] Implement biometric auth
- [ ] Performance optimization
- [ ] Accessibility audit
- [ ] Localization (Spanish primary)

---

## 13. API Response Conventions

All API responses follow this structure:

```kotlin
// Success response
{
    "success": true,
    "data": { ... }
}

// Error response
{
    "success": false,
    "error": {
        "code": "VALIDATION_ERROR",
        "message": "Human-readable message",
        "details": { ... }
    }
}

// List response
{
    "success": true,
    "data": {
        "items": [...],
        "total": 100,
        "page": 1,
        "pageSize": 20
    }
}
```

---

## 14. Permissions System

### 14.1 Fraccio Permissions

| Permission | Description |
|------------|-------------|
| `admin.manage` | Full admin access |
| `admin.read` | Read-only admin access |
| `resident.read` | Resident access |
| `finances.read` | View financial data |
| `finances.manage` | Manage charges/payments |
| `community.read` | View community data |
| `community.manage` | Manage houses/streets/users |
| `settings.read` | View settings |
| `settings.manage` | Manage settings |

### 14.2 Permission Checking

```kotlin
// In shared module
fun hasPermission(permission: String): Boolean {
    return syncState.user?.permissions?.contains(permission) == true
}

fun hasAnyPermission(vararg permissions: String): Boolean {
    return permissions.any { hasPermission(it) }
}
```

---

## 15. Error Handling

### 15.1 Error Types

```kotlin
sealed class AppError : Exception() {
    data class Network(override val message: String) : AppError()
    data class Auth(val code: String, override val message: String) : AppError()
    data class Validation(val fields: Map<String, String>) : AppError()
    data class Server(val code: String, override val message: String) : AppError()
    data object Unknown : AppError()
}
```

### 15.2 Error Display

- Network errors: Retry option with offline indicator
- Auth errors: Redirect to login
- Validation errors: Inline field errors
- Server errors: Toast with message
- Unknown errors: Generic message with support contact

---

*This specification is based on the existing React web applications and should be used as the foundation for the KMP mobile implementation.*
