# Architecture Reorganization Plan

## Current Structure
```
shared/src/commonMain/kotlin/com/dadomatch/shared/
├── core/
├── data/
│   ├── local/
│   ├── mapper/
│   ├── remote/
│   └── repository/
├── di/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
└── presentation/
    ├── mvi/
    ├── ui/
    └── viewmodel/
```

## Proposed Feature-Based Structure
```
shared/src/commonMain/kotlin/com/dadomatch/shared/
├── core/                           # Shared core utilities
│   ├── di/                         # Core DI module
│   ├── data/                       # Core data (DataStore, Database)
│   └── util/                       # Shared utilities
│
├── feature/
│   ├── icebreaker/                 # Icebreaker generation feature
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   └── GeminiService.kt
│   │   │   └── repository/
│   │   │       └── IcebreakerRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── IcebreakerFeedback.kt
│   │   │   ├── repository/
│   │   │   │   └── IcebreakerRepository.kt
│   │   │   └── usecase/
│   │   │       ├── GenerateIcebreakerUseCase.kt
│   │   │       ├── SubmitFeedbackUseCase.kt
│   │   │       └── RollDiceUseCase.kt
│   │   ├── presentation/
│   │   │   ├── viewmodel/
│   │   │   │   └── HomeViewModel.kt
│   │   │   └── ui/
│   │   │       ├── HomeScreen.kt
│   │   │       ├── components/
│   │   │       │   ├── IcebreakerDialog.kt
│   │   │       │   ├── ActionChoiceDialog.kt
│   │   │       │   ├── FeedbackDialog.kt
│   │   │       │   ├── Rizz.kt
│   │   │       │   └── SelectorGroup.kt
│   │   └── di/
│   │       └── IcebreakerModule.kt
│   │
│   ├── subscription/               # Subscription/Paywall feature
│   │   ├── data/
│   │   │   ├── remote/
│   │   │   │   └── RevenueCatService.kt
│   │   │   ├── local/
│   │   │   │   └── SubscriptionLocalDataSource.kt
│   │   │   ├── mapper/
│   │   │   │   └── SubscriptionMapper.kt
│   │   │   └── repository/
│   │   │       └── SubscriptionRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Product.kt
│   │   │   │   ├── SubscriptionStatus.kt
│   │   │   │   ├── SubscriptionTier.kt
│   │   │   │   └── Entitlement.kt
│   │   │   ├── repository/
│   │   │   │   └── SubscriptionRepository.kt
│   │   │   └── usecase/
│   │   │       ├── CheckEntitlementUseCase.kt
│   │   │       ├── GetAvailableProductsUseCase.kt
│   │   │       ├── GetSubscriptionStatusUseCase.kt
│   │   │       ├── PurchaseSubscriptionUseCase.kt
│   │   │       └── RestorePurchasesUseCase.kt
│   │   ├── presentation/
│   │   │   └── ui/
│   │   │       ├── PaywallScreen.kt
│   │   │       ├── SettingsScreen.kt
│   │   │       └── components/
│   │   │           └── SubscriptionCard.kt
│   │   └── di/
│   │       └── SubscriptionModule.kt
│   │
│   ├── success/                    # Success tracking feature
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── dao/
│   │   │   │   │   └── SuccessDao.kt
│   │   │   │   └── entity/
│   │   │   │       └── SuccessEntity.kt
│   │   │   ├── mapper/
│   │   │   │   └── SuccessMapper.kt
│   │   │   └── repository/
│   │   │       └── SuccessRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── SuccessRecord.kt
│   │   │   ├── repository/
│   │   │   │   └── SuccessRepository.kt
│   │   │   └── usecase/
│   │   │       ├── AddSuccessUseCase.kt
│   │   │       └── GetSuccessesUseCase.kt
│   │   ├── presentation/
│   │   │   └── ui/
│   │   │       └── SuccessesScreen.kt
│   │   └── di/
│   │       └── SuccessModule.kt
│   │
│   └── onboarding/                 # Onboarding feature
│       ├── data/
│       │   └── repository/
│       │       └── PreferenceRepositoryImpl.kt
│       ├── domain/
│       │   ├── repository/
│       │   │   └── PreferenceRepository.kt
│       │   └── usecase/
│       │       ├── GetOnboardingStatusUseCase.kt
│       │       └── SetOnboardingStatusUseCase.kt
│       ├── presentation/
│       │   └── ui/
│       │       └── components/
│       │           └── OnboardingScreen.kt
│       └── di/
│           └── OnboardingModule.kt
│
└── presentation/                   # Shared presentation layer
    ├── navigation/
    │   ├── AppNavigation.kt
    │   └── Screen.kt
    ├── theme/
    │   ├── AppConstants.kt
    │   ├── Color.kt
    │   └── Theme.kt
    └── ui/
        └── components/             # Shared components
            ├── EmptyState.kt
            ├── Confetti.kt
            └── LiquidFooterMenu.kt
```

## Migration Steps

### Phase 1: Create Feature Directories
1. Create feature module directories
2. Create subdirectories for each layer (data, domain, presentation, di)

### Phase 2: Move Icebreaker Feature
1. Move icebreaker-related models, repositories, use cases
2. Move HomeViewModel and HomeScreen
3. Move icebreaker-specific UI components
4. Update IcebreakerModule

### Phase 3: Move Subscription Feature
1. Move subscription models, repositories, use cases
2. Move PaywallScreen, SettingsScreen
3. Move SubscriptionCard component
4. Update SubscriptionModule

### Phase 4: Move Success Feature
1. Move success models, repositories, use cases
2. Move SuccessesScreen
3. Update SuccessModule

### Phase 5: Move Onboarding Feature
1. Move onboarding repositories and use cases
2. Move OnboardingScreen
3. Create OnboardingModule

### Phase 6: Organize Core
1. Move shared data sources (DataStore, Database) to core
2. Keep shared UI components in presentation/ui/components
3. Update CoreModule

### Phase 7: Update Imports
1. Update all import statements across the project
2. Update DI module references

## Benefits
- **Better Separation of Concerns**: Each feature is self-contained
- **Easier Navigation**: Related code is grouped together
- **Scalability**: Easy to add new features
- **Testability**: Each feature can be tested independently
- **Team Collaboration**: Different teams can work on different features
- **Code Reusability**: Clear distinction between feature-specific and shared code
