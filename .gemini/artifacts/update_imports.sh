#!/bin/bash

# Script to update imports across the entire codebase

echo "Updating imports across the codebase..."

# Find all Kotlin files
find shared/src/commonMain/kotlin -name "*.kt" -type f -exec sed -i '' \
  -e 's/import com\.dadomatch\.shared\.core\.Resource/import com.dadomatch.shared.core.util.Resource/g' \
  -e 's/import com\.dadomatch\.shared\.data\.remote\.GeminiService/import com.dadomatch.shared.feature.icebreaker.data.remote.GeminiService/g' \
  -e 's/import com\.dadomatch\.shared\.data\.remote\.RevenueCatService/import com.dadomatch.shared.feature.subscription.data.remote.RevenueCatService/g' \
  -e 's/import com\.dadomatch\.shared\.data\.local\.AppDatabase/import com.dadomatch.shared.core.data.AppDatabase/g' \
  -e 's/import com\.dadomatch\.shared\.data\.local\.createDataStore/import com.dadomatch.shared.core.data.createDataStore/g' \
  -e 's/import com\.dadomatch\.shared\.data\.local\.getDatabaseBuilder/import com.dadomatch.shared.core.data.getDatabaseBuilder/g' \
  -e 's/import com\.dadomatch\.shared\.data\.local\.SubscriptionLocalDataSource/import com.dadomatch.shared.feature.subscription.data.local.SubscriptionLocalDataSource/g' \
  -e 's/import com\.dadomatch\.shared\.data\.local\.dao\.SuccessDao/import com.dadomatch.shared.feature.success.data.local.dao.SuccessDao/g' \
  -e 's/import com\.dadomatch\.shared\.data\.local\.entity\.SuccessEntity/import com.dadomatch.shared.feature.success.data.local.entity.SuccessEntity/g' \
  -e 's/import com\.dadomatch\.shared\.data\.mapper\.SubscriptionMapper/import com.dadomatch.shared.feature.subscription.data.mapper.SubscriptionMapper/g' \
  -e 's/import com\.dadomatch\.shared\.data\.mapper\.SuccessMapper/import com.dadomatch.shared.feature.success.data.mapper.SuccessMapper/g' \
  -e 's/import com\.dadomatch\.shared\.data\.repository\.IcebreakerRepositoryImpl/import com.dadomatch.shared.feature.icebreaker.data.repository.IcebreakerRepositoryImpl/g' \
  -e 's/import com\.dadomatch\.shared\.data\.repository\.SubscriptionRepositoryImpl/import com.dadomatch.shared.feature.subscription.data.repository.SubscriptionRepositoryImpl/g' \
  -e 's/import com\.dadomatch\.shared\.data\.repository\.SuccessRepositoryImpl/import com.dadomatch.shared.feature.success.data.repository.SuccessRepositoryImpl/g' \
  -e 's/import com\.dadomatch\.shared\.data\.repository\.PreferenceRepositoryImpl/import com.dadomatch.shared.feature.onboarding.data.repository.PreferenceRepositoryImpl/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.model\.IcebreakerFeedback/import com.dadomatch.shared.feature.icebreaker.domain.model.IcebreakerFeedback/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.model\.Product/import com.dadomatch.shared.feature.subscription.domain.model.Product/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.model\.SubscriptionStatus/import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionStatus/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.model\.SubscriptionTier/import com.dadomatch.shared.feature.subscription.domain.model.SubscriptionTier/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.model\.Entitlement/import com.dadomatch.shared.feature.subscription.domain.model.Entitlement/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.model\.SuccessRecord/import com.dadomatch.shared.feature.success.domain.model.SuccessRecord/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.repository\.IcebreakerRepository/import com.dadomatch.shared.feature.icebreaker.domain.repository.IcebreakerRepository/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.repository\.SubscriptionRepository/import com.dadomatch.shared.feature.subscription.domain.repository.SubscriptionRepository/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.repository\.SuccessRepository/import com.dadomatch.shared.feature.success.domain.repository.SuccessRepository/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.repository\.PreferenceRepository/import com.dadomatch.shared.feature.onboarding.domain.repository.PreferenceRepository/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.GenerateIcebreakerUseCase/import com.dadomatch.shared.feature.icebreaker.domain.usecase.GenerateIcebreakerUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.SubmitFeedbackUseCase/import com.dadomatch.shared.feature.icebreaker.domain.usecase.SubmitFeedbackUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.RollDiceUseCase/import com.dadomatch.shared.feature.icebreaker.domain.usecase.RollDiceUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.NoRollsRemainingException/import com.dadomatch.shared.feature.icebreaker.domain.usecase.NoRollsRemainingException/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.CheckEntitlementUseCase/import com.dadomatch.shared.feature.subscription.domain.usecase.CheckEntitlementUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.GetAvailableProductsUseCase/import com.dadomatch.shared.feature.subscription.domain.usecase.GetAvailableProductsUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.GetSubscriptionStatusUseCase/import com.dadomatch.shared.feature.subscription.domain.usecase.GetSubscriptionStatusUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.PurchaseSubscriptionUseCase/import com.dadomatch.shared.feature.subscription.domain.usecase.PurchaseSubscriptionUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.RestorePurchasesUseCase/import com.dadomatch.shared.feature.subscription.domain.usecase.RestorePurchasesUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.AddSuccessUseCase/import com.dadomatch.shared.feature.success.domain.usecase.AddSuccessUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.GetSuccessesUseCase/import com.dadomatch.shared.feature.success.domain.usecase.GetSuccessesUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.GetOnboardingStatusUseCase/import com.dadomatch.shared.feature.onboarding.domain.usecase.GetOnboardingStatusUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.domain\.usecase\.SetOnboardingStatusUseCase/import com.dadomatch.shared.feature.onboarding.domain.usecase.SetOnboardingStatusUseCase/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.viewmodel\.HomeViewModel/import com.dadomatch.shared.feature.icebreaker.presentation.viewmodel.HomeViewModel/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.screens\.HomeScreen/import com.dadomatch.shared.feature.icebreaker.presentation.ui.HomeScreen/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.screens\.PaywallScreen/import com.dadomatch.shared.feature.subscription.presentation.ui.PaywallScreen/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.screens\.SettingsScreen/import com.dadomatch.shared.feature.subscription.presentation.ui.SettingsScreen/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.screens\.SuccessesScreen/import com.dadomatch.shared.feature.success.presentation.ui.SuccessesScreen/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.components\.IcebreakerDialog/import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.IcebreakerDialog/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.components\.ActionChoiceDialog/import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.ActionChoiceDialog/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.components\.FeedbackDialog/import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.FeedbackDialog/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.components\.RizzDice/import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.RizzDice/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.components\.SelectorGroup/import com.dadomatch.shared.feature.icebreaker.presentation.ui.components.SelectorGroup/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.components\.SubscriptionCard/import com.dadomatch.shared.feature.subscription.presentation.ui.components.SubscriptionCard/g' \
  -e 's/import com\.dadomatch\.shared\.presentation\.ui\.components\.OnboardingScreen/import com.dadomatch.shared.feature.onboarding.presentation.ui.components.OnboardingScreen/g' \
  {} \;

echo "Imports updated successfully!"
