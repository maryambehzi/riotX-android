/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotx.features.login

import im.vector.riotx.core.platform.VectorSharedAction

// Supported navigation actions for LoginActivity
sealed class LoginNavigation : VectorSharedAction {
    object OpenServerSelection : LoginNavigation()
    object OnServerSelectionDone : LoginNavigation()
    object OnLoginFlowRetrieved : LoginNavigation()
    object OnSignModeSelected : LoginNavigation()
    object OnForgetPasswordClicked : LoginNavigation()
    object OnResetPasswordSendThreePidDone : LoginNavigation()
    object OnResetPasswordMailConfirmationSuccess : LoginNavigation()
    object OnResetPasswordMailConfirmationSuccessDone : LoginNavigation()

    data class OnSendEmailSuccess(val email: String) : LoginNavigation()
    data class OnSendMsisdnSuccess(val msisdn: String) : LoginNavigation()

    data class OnWebLoginError(val errorCode: Int, val description: String, val failingUrl: String) : LoginNavigation()
}
