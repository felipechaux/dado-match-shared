#!/bin/bash

# Script to update package declarations in reorganized files

# Update Icebreaker feature
find shared/src/commonMain/kotlin/com/dadomatch/shared/feature/icebreaker -name "*.kt" -type f -exec sed -i '' \
  -e 's/package com\.dadomatch\.shared\.data\.remote/package com.dadomatch.shared.feature.icebreaker.data.remote/g' \
  -e 's/package com\.dadomatch\.shared\.data\.repository/package com.dadomatch.shared.feature.icebreaker.data.repository/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.model/package com.dadomatch.shared.feature.icebreaker.domain.model/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.repository/package com.dadomatch.shared.feature.icebreaker.domain.repository/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.usecase/package com.dadomatch.shared.feature.icebreaker.domain.usecase/g' \
  -e 's/package com\.dadomatch\.shared\.presentation\.viewmodel/package com.dadomatch.shared.feature.icebreaker.presentation.viewmodel/g' \
  -e 's/package com\.dadomatch\.shared\.presentation\.ui\.screens/package com.dadomatch.shared.feature.icebreaker.presentation.ui/g' \
  -e 's/package com\.dadomatch\.shared\.presentation\.ui\.components/package com.dadomatch.shared.feature.icebreaker.presentation.ui.components/g' \
  -e 's/package com\.dadomatch\.shared\.di/package com.dadomatch.shared.feature.icebreaker.di/g' \
  {} \;

# Update Subscription feature
find shared/src/commonMain/kotlin/com/dadomatch/shared/feature/subscription -name "*.kt" -type f -exec sed -i '' \
  -e 's/package com\.dadomatch\.shared\.data\.remote/package com.dadomatch.shared.feature.subscription.data.remote/g' \
  -e 's/package com\.dadomatch\.shared\.data\.local/package com.dadomatch.shared.feature.subscription.data.local/g' \
  -e 's/package com\.dadomatch\.shared\.data\.mapper/package com.dadomatch.shared.feature.subscription.data.mapper/g' \
  -e 's/package com\.dadomatch\.shared\.data\.repository/package com.dadomatch.shared.feature.subscription.data.repository/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.model/package com.dadomatch.shared.feature.subscription.domain.model/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.repository/package com.dadomatch.shared.feature.subscription.domain.repository/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.usecase/package com.dadomatch.shared.feature.subscription.domain.usecase/g' \
  -e 's/package com\.dadomatch\.shared\.presentation\.ui\.screens/package com.dadomatch.shared.feature.subscription.presentation.ui/g' \
  -e 's/package com\.dadomatch\.shared\.presentation\.ui\.components/package com.dadomatch.shared.feature.subscription.presentation.ui.components/g' \
  -e 's/package com\.dadomatch\.shared\.di/package com.dadomatch.shared.feature.subscription.di/g' \
  {} \;

# Update Success feature
find shared/src/commonMain/kotlin/com/dadomatch/shared/feature/success -name "*.kt" -type f -exec sed -i '' \
  -e 's/package com\.dadomatch\.shared\.data\.local\.dao/package com.dadomatch.shared.feature.success.data.local.dao/g' \
  -e 's/package com\.dadomatch\.shared\.data\.local\.entity/package com.dadomatch.shared.feature.success.data.local.entity/g' \
  -e 's/package com\.dadomatch\.shared\.data\.mapper/package com.dadomatch.shared.feature.success.data.mapper/g' \
  -e 's/package com\.dadomatch\.shared\.data\.repository/package com.dadomatch.shared.feature.success.data.repository/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.model/package com.dadomatch.shared.feature.success.domain.model/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.repository/package com.dadomatch.shared.feature.success.domain.repository/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.usecase/package com.dadomatch.shared.feature.success.domain.usecase/g' \
  -e 's/package com\.dadomatch\.shared\.presentation\.ui\.screens/package com.dadomatch.shared.feature.success.presentation.ui/g' \
  -e 's/package com\.dadomatch\.shared\.di/package com.dadomatch.shared.feature.success.di/g' \
  {} \;

# Update Onboarding feature
find shared/src/commonMain/kotlin/com/dadomatch/shared/feature/onboarding -name "*.kt" -type f -exec sed -i '' \
  -e 's/package com\.dadomatch\.shared\.data\.repository/package com.dadomatch.shared.feature.onboarding.data.repository/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.repository/package com.dadomatch.shared.feature.onboarding.domain.repository/g' \
  -e 's/package com\.dadomatch\.shared\.domain\.usecase/package com.dadomatch.shared.feature.onboarding.domain.usecase/g' \
  -e 's/package com\.dadomatch\.shared\.presentation\.ui\.components/package com.dadomatch.shared.feature.onboarding.presentation.ui.components/g' \
  {} \;

# Update Core
find shared/src/commonMain/kotlin/com/dadomatch/shared/core -name "*.kt" -type f -exec sed -i '' \
  -e 's/package com\.dadomatch\.shared\.data\.local/package com.dadomatch.shared.core.data/g' \
  -e 's/package com\.dadomatch\.shared\.core$/package com.dadomatch.shared.core.util/g' \
  -e 's/package com\.dadomatch\.shared\.di/package com.dadomatch.shared.core.di/g' \
  {} \;

echo "Package declarations updated successfully!"
