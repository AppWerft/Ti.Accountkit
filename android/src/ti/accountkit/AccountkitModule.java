package ti.accountkit;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiActivityResultHandler;

import android.app.Activity;
import android.content.Intent;

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

	Activity activity;
	private static final String LCAT = "TiaccountkitModule";

	public AccountkitModule() {
		super();
	}

	@Kroll.method
	public void initialize() {
		activity = TiApplication.getAppRootOrCurrentActivity();
		AccountKit.initialize(TiApplication.getInstance()
				.getApplicationContext());
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		Log.d(LCAT, "inside onAppCreate");
		// put module init code that needs to run when the application is
		// created
	}

	@Kroll.method
	public void loginWithPhone() {
		final Intent intent = new Intent(getActivity(),
				AccountKitActivity.class);
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
