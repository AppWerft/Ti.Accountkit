package ti.accountkit;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiConvert;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

@Kroll.module(name = "Accountkit", id = "ti.accountkit")
public class AccountkitModule extends KrollModule implements
		TiActivityResultHandler {
	public static int APP_REQUEST_CODE = 99;
	@Kroll.constant
	public static final int RESPONSE_TYPE_AUTHORIZATION_CODE = 0;
	@Kroll.constant
	public static final int RESPONSE_TYPE_AUTHORIZATION_TOKEN = 1;
	public static final int MSG_PHONE_LOGIN = 0;
	Activity activity;
	private static final String LCAT = "TiaccountkitModule";

	public AccountkitModule() {
		super();
		activity = TiApplication.getAppRootOrCurrentActivity();

	}

	@Kroll.method
	public void logout() {
		AccountKit.logOut();
	}

	@Kroll.method
	public void initialize() {
		/*
		 * Log.d(LCAT, "start initialize inside krollmethod"); if
		 * (AccountKit.isInitialized() == false) { Log.d(LCAT,
		 * "start initialize inside krollmethod after questio n if init");
		 * activity = TiApplication.getAppRootOrCurrentActivity(); Log.d(LCAT,
		 * "Kit initialized after question");
		 * AccountKit.initialize(TiApplication.getInstance()
		 * .getApplicationContext()); Log.d(LCAT, "isInitialized=" +
		 * AccountKit.isInitialized()); }
		 */
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		AccountKit.initialize(TiApplication.getInstance()
				.getApplicationContext());
	}

	@Kroll.method
	public void loginWithPhone() {
		activity = TiApplication.getAppRootOrCurrentActivity();
		Log.d(LCAT,
				"TiApplication.isUIThread() = " + TiApplication.isUIThread());

		if (!TiApplication.isUIThread()) {
			TiMessenger.sendBlockingMainMessage(new Handler(TiMessenger
					.getMainMessenger().getLooper(), new Handler.Callback() {
				public boolean handleMessage(Message msg) {
					switch (msg.what) {
					case MSG_PHONE_LOGIN: {
						AsyncResult result = (AsyncResult) msg.obj;
						result.setResult(null);
						loginWithPhone_forced_in_UIThread();
						return true;
					}
					}
					return false;
				}
			}).obtainMessage(MSG_PHONE_LOGIN));
		} else {
			loginWithPhone_forced_in_UIThread();
		}
	}

	private void loginWithPhone_forced_in_UIThread() {
		if (false == AccountKit.isInitialized()) {
			AccountKit.initialize(TiApplication.getInstance()
					.getApplicationContext());
			Log.d(LCAT,
					"Init inside krollmethod loginwithphone "
							+ AccountKit.isInitialized());
		}
		Log.d(LCAT,
				"TiApplication.isUIThread() = " + TiApplication.isUIThread());

		final Intent intent = new Intent(getActivity(),
				AccountKitActivity.class);
		// Initialization error: 501: The SDK has not been initialized, make
		// sure to call AccountKit.initializeSdk() first
		AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(
				LoginType.PHONE, AccountKitActivity.ResponseType.CODE); // or
																		// .ResponseType.TOKEN
		// ... perform additional configuration ...
		intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
				configurationBuilder.build());

		activity.startActivityForResult(intent, APP_REQUEST_CODE);

	}

	@Kroll.method
	public void loginWithEmail() {
		final Intent intent = new Intent(getActivity(),
				AccountKitActivity.class);
		AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(
				LoginType.EMAIL, AccountKitActivity.ResponseType.CODE); // or
		intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
				configurationBuilder.build());
		activity = TiApplication.getAppRootOrCurrentActivity();
		activity.startActivityForResult(intent, APP_REQUEST_CODE);
	}

	@Override
	public void onError(Activity arg0, int arg1, Exception arg2) {
	}

	@Override
	public void onResult(Activity activity, final int requestCode,
			final int resultCode, final Intent data) {
		if (requestCode == APP_REQUEST_CODE && hasListeners("login")) {
			KrollDict result = new KrollDict();
			AccountKitLoginResult loginResult = data
					.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
			if (loginResult.getError() != null) {
				result.put("success", false);
				result.put("error", loginResult.getError());
				// handling of error
			} else if (loginResult.wasCancelled()) {
				// toastMessage = "Login Cancelled";
			} else {
				if (loginResult.getAccessToken() != null) {
					result.put("success", true);
					result.put("accesstoken", loginResult.getAccessToken()
							.getAccountId());

				} else {
					result.put("success", true);
					result.put("code", loginResult.getAuthorizationCode()
							.substring(0, 10));
				}
			}
			fireEvent("login", result);
		}

	}

}
