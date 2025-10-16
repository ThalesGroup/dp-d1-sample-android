package com.thalesgroup.d1.physicalcard;

import com.thalesgroup.d1.physicalcard.model.D1PhysicalCardApi;
import com.thalesgroup.d1.physicalcard.model.D1PhysicalCardChangePinListener;
import com.thalesgroup.d1.physicalcard.model.D1PhysicalCardListener;
import com.thalesgroup.d1.templates.core.Constants;
import com.thalesgroup.d1.templates.core.D1Core;
import com.thalesgroup.d1.templates.core.model.D1ModuleConnector;
import com.thalesgroup.gemalto.d1.CardPINUI;
import com.thalesgroup.gemalto.d1.ChangePINOptions;
import com.thalesgroup.gemalto.d1.D1Exception;
import com.thalesgroup.gemalto.d1.D1Params;
import com.thalesgroup.gemalto.d1.D1Task;
import com.thalesgroup.gemalto.d1.EntryUI;
import com.thalesgroup.gemalto.d1.PINDisplayTextView;
import com.thalesgroup.gemalto.d1.PINEntryUI;
import com.thalesgroup.gemalto.d1.SecureEditText;
import com.thalesgroup.gemalto.d1.card.CardActivationMethod;

import androidx.annotation.NonNull;

public class D1PhysicalCard implements D1PhysicalCardApi {

    private static final D1PhysicalCard INSTANCE = new D1PhysicalCard();

    /**
     * Returns singleton of public API for application use.
     *
     * @return {@code D1PhysicalCardApi} instance.
     */
    @NonNull
    public static D1PhysicalCardApi getInstance() {
        return INSTANCE;
    }

    @Override
    public void activatePhysicalCard(@NonNull final String cardId,
                                     @NonNull final SecureEditText secureTextEdit,
                                     @NonNull final D1PhysicalCardListener listener) {
        final EntryUI entryUI = new EntryUI(secureTextEdit);
        getTask().activatePhysicalCard(cardId, entryUI, new D1Task.Callback<Void>() {
            @Override
            public void onSuccess(final Void unused) {
                listener.onActivatePhysicalCardSuccess();
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                listener.onPhysicalCardOperationError(exception);
            }
        });
    }

    @Override
    public void getPhysicalCardPin(@NonNull final String cardId,
                                   @NonNull final PINDisplayTextView pinDisplayTextView,
                                   @NonNull final D1PhysicalCardListener listener) {
        final CardPINUI cardPINUI = new CardPINUI(pinDisplayTextView);
        getTask().displayPhysicalCardPIN(cardId, cardPINUI, new D1Task.Callback<Void>() {
            @Override
            public void onSuccess(final Void unused) {
                listener.onGetPhysicalCardPinSuccess();
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                listener.onPhysicalCardOperationError(exception);
            }
        });
    }

    @Override
    public void getActivationMethod(@NonNull final String cardId, @NonNull final D1PhysicalCardListener listener) {
        getTask().getCardActivationMethod(cardId, new D1Task.Callback<CardActivationMethod>() {
            @Override
            public void onSuccess(final CardActivationMethod cardActivationMethod) {
                listener.onGetActivationMethodSuccess(cardActivationMethod);
            }

            @Override
            public void onError(@NonNull final D1Exception exception) {
                listener.onPhysicalCardOperationError(exception);
            }
        });
    }

    @Override
    public void changePin(@NonNull final String cardId,
                          @NonNull final SecureEditText enterPinSecureTextEdit,
                          @NonNull final SecureEditText confirmPinSecureTextEdit,
                          @NonNull final D1PhysicalCardChangePinListener listener) {
        final D1SubmitPinChangeImpl d1SubmitPinChange = new D1SubmitPinChangeImpl(listener);
        final PINEntryUI.PINEventListener pinEventListener = (pinEvent, s) -> {
            switch (pinEvent) {
                case FIRST_ENTRY_FINISH:
                    listener.onFirstEntryFinished();
                    break;
                case PIN_MISMATCH:
                    listener.onPinMismatch();
                    break;
                case PIN_MATCH:
                    listener.onPinMatch(d1SubmitPinChange);
                    break;
                default:
                    break;
            }
        };

        final ChangePINOptions options = new ChangePINOptions(4);
        final PINEntryUI pinEntryUI = getTask().changePIN(cardId,
                                                          enterPinSecureTextEdit,
                                                          confirmPinSecureTextEdit,
                                                          options,
                                                          pinEventListener);
        d1SubmitPinChange.setPINEntryUI(pinEntryUI);
    }

    @Override
    public D1ModuleConnector createModuleConnector() {
        return new PhysicalCardModuleConnector();
    }

    /**
     * Retrieves the {@code D1Task}.
     *
     * @return {@code D1Task} or {@code IllegalStateException} if D1 SDK was not yet configured.
     */
    private D1Task getTask() {
        return D1Core.getInstance().getD1Task();
    }

    /**
     * D1PhysicalCard related {@code D1ModuleConnector}.
     */
    private static class PhysicalCardModuleConnector implements D1ModuleConnector {

        @Override
        public D1Params getConfiguration() {
            // There is no extra configuration needed for this module.
            return null;
        }

        @Override
        public Constants.Module getModuleId() {
            return Constants.Module.PHYSICAL_CARD;
        }
    }

    /**
     * {@code D1PhysicalCardChangePinListener.D1SubmitPinChange} implementation.
     */
    private static class D1SubmitPinChangeImpl implements D1PhysicalCardChangePinListener.D1SubmitPinChange {
        private PINEntryUI mPINEntryUI;
        private final D1PhysicalCardChangePinListener mD1PhysicalCardChangePinListener;

        public D1SubmitPinChangeImpl(final D1PhysicalCardChangePinListener d1PhysicalCardChangePinListener) {
            mD1PhysicalCardChangePinListener = d1PhysicalCardChangePinListener;
        }

        @Override
        public void submitPinChange() {
            mPINEntryUI.submit(new D1Task.Callback<Void>() {
                @Override
                public void onSuccess(final Void unused) {
                    mD1PhysicalCardChangePinListener.onChangePinSuccess();
                }

                @Override
                public void onError(@NonNull final D1Exception exception) {
                    mD1PhysicalCardChangePinListener.onChangePinErrorError(exception);
                }
            });
        }

        public void setPINEntryUI(final PINEntryUI PINEntryUI) {
            mPINEntryUI = PINEntryUI;
        }
    }
}
