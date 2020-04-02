/*
 * Copyright (c) 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotx.features.crypto.recover

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import im.vector.matrix.android.internal.crypto.model.rest.UserPasswordAuth
import im.vector.riotx.R
import im.vector.riotx.core.di.ScreenComponent
import im.vector.riotx.core.dialogs.PromptPasswordDialog
import im.vector.riotx.core.extensions.commitTransaction
import im.vector.riotx.core.platform.VectorBaseBottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_bootstrap.*
import javax.inject.Inject
import kotlin.reflect.KClass

class BootstrapBottomSheet : VectorBaseBottomSheetDialogFragment() {

    override val showExpanded = true

    @Inject
    lateinit var bootstrapViewModelFactory: BootstrapSharedViewModel.Factory

    private val viewModel by fragmentViewModel(BootstrapSharedViewModel::class)

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun getLayoutResId() = R.layout.bottom_sheet_bootstrap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observeViewEvents { event ->
            when (event) {
                is BootstrapViewEvents.Dismiss         -> dismiss()
                is BootstrapViewEvents.ModalError      -> {
                    AlertDialog.Builder(requireActivity())
                            .setTitle(R.string.dialog_title_error)
                            .setMessage(event.error)
                            .setPositiveButton(R.string.ok, null)
                            .show()
                }
                BootstrapViewEvents.RecoveryKeySaved   -> {
                    KeepItSafeDialog().show(requireActivity())
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnKeyListener { _, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                    viewModel.handle(BootstrapActions.GoBack)
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->

        when (state.step) {
            is BootstrapStep.SetupPassphrase   -> {
                bootstrapIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_message_password))
                bootstrapTitleText.text = getString(R.string.set_recovery_passphrase, getString(R.string.recovery_passphrase))
                showFragment(BootstrapEnterPassphraseFragment::class, Bundle())
            }
            is BootstrapStep.ConfirmPassphrase -> {
                bootstrapIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_message_password))
                bootstrapTitleText.text = getString(R.string.confirm_recovery_passphrase, getString(R.string.recovery_passphrase))
                showFragment(BootstrapConfirmPassphraseFragment::class, Bundle())
            }
            is BootstrapStep.AccountPassword -> {
                bootstrapIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_user))
                bootstrapTitleText.text = getString(R.string.account_password)
                showFragment(BootstrapAccountPasswordFragment::class, Bundle())
            }
            is BootstrapStep.Initializing      -> {
                bootstrapIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_message_key))
                bootstrapTitleText.text = getString(R.string.bootstrap_loading_title)
                showFragment(BootstrapWaitingFragment::class, Bundle())
            }
            is BootstrapStep.SaveRecoveryKey   -> {
                bootstrapIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_message_key))
                bootstrapTitleText.text = getString(R.string.keys_backup_setup_step3_please_make_copy)
                showFragment(BootstrapSaveRecoveryKeyFragment::class, Bundle())
            }
            is BootstrapStep.DoneSuccess       -> {
                bootstrapIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_message_key))
                bootstrapTitleText.text = getString(R.string.bootstrap_finish_title)
                showFragment(BootstrapConclusionFragment::class, Bundle())
            }
        }
        super.invalidate()
    }

    private fun showFragment(fragmentClass: KClass<out Fragment>, bundle: Bundle) {
        if (childFragmentManager.findFragmentByTag(fragmentClass.simpleName) == null) {
            childFragmentManager.commitTransaction {
                replace(R.id.bottomSheetFragmentContainer,
                        fragmentClass.java,
                        bundle,
                        fragmentClass.simpleName
                )
            }
        }
    }
}