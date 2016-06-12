package ti.accountkit;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiLifecycle.OnActivityResultEvent;

import android.app.Activity;
import android.content.Intent;

import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

@Kroll.module(name = "Accountkit", id = "ti.accountkit")
public class AccountkitModule extends KrollModule implements
		OnActivityResultEvent {
	public static int APP_REQUEST_CODE = 99;
	Activity activity;
	private static final String LCAT = "TiaccountkitModule";

	public AccountkitModule() {
		super();
	}

	@Kroll.method
	public void initialize() {
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
		activity = TiApplication.getAppRootOrCurrentActivity();
		activity.startActivityForResult(intent, APP_REQUEST_CODE);

	}

	@Kroll.method
	public void loginWithEmail() {
		final Intent intent = new Intent(getActivity(),
				AccountKitActivity.class);
		AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(
				LoginType.EMAIL, AccountKitActivity.ResponseType.CODE); // or
																		// .ResponseType.TOKEN
		// ... perform additional configuration ...
		intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
				configurationBuilder.build());
		activity = TiApplication.getAppRootOrCurrentActivity();
		activity.startActivityForResult(intent, APP_REQUEST_CODE);

	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == APP_REQUEST_CODE) { // confirm that this response
												// matches your request
			AccountKitLoginResult loginResult = data
					.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
			String toastMessage;
			if (loginResult.getError() != null) {
				toastMessage = loginResult.getError().getErrorType()
						.getMessage();
				showErrorActivity(loginResult.getError());
			} else if (loginResult.wasCancelled()) {
				toastMessage = "Login Cancelled";
			} else {
				if (loginResult.getAccessToken() != null) {

				} else {

				}

				// If you have an authorization code, retrieve it from
				// loginResult.getAuthorizationCode()
				// and pass it to your server and exchange it for an access
				// token.

				// Success! Start your next activity...

			}

			// Surface the result to your user in an appropriate way.

		}
	}

}
