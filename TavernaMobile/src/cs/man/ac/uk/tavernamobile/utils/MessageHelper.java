package cs.man.ac.uk.tavernamobile.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;

public class MessageHelper extends AlertDialog {

	// Dialog Message String
	private String dialogMessage = "Unknown dialog message. Please contact developer for support.";
	private Context dialogContext;
	
	// Constructor which takes the input message string
	// that used to display
	public MessageHelper(Context context, String message) {
		super(context);	
		if (message != null){
			this.dialogMessage = message;
		}		
		this.dialogContext = context;
	}
	
	// Method to show general message dialog 
	public static void showMessageDialog(Context context, String message){
		new MessageHelper(context, message).neutralDialog().show();
	}

	// Method to show dialog with 2 options
	public static void showOptionsDialog(Context context, String message, String title, 
			CallbackTask listenerPos, CallbackTask listenerNeg){
		new MessageHelper(context, message).twoOptionsDialog(title, listenerPos, listenerNeg).show();
	}

	// Method to create the dialog
	private AlertDialog neutralDialog() {
		ContextThemeWrapper ctw = new ContextThemeWrapper(dialogContext, R.style.DefaultTheme );
        AlertDialog.Builder builder = new AlertDialog.Builder( ctw );
		//Builder builder = new Builder(this.dialogContext);
		/*builder.setMessage(dialogMessage)
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// dismiss the dialog
				dialog.dismiss();
			}
		});*/
		
		View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout_neutral, null);
		//dialog.setContentView(R.layout.neutral_dialog_layout);
		// set up dialog message
		TextView dialogMessageTextView = (TextView) dialogView.findViewById(R.id.neutraldialog_message);
		dialogMessageTextView.setText(dialogMessage);
		// set up button action
		Button dialogButton = (Button) dialogView.findViewById(R.id.neutraldialog_dismiss_button);
		builder.setView(dialogView);
		
		final AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		dialogButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		return dialog;
	}

	private AlertDialog twoOptionsDialog(String title, final CallbackTask listenerPos, final CallbackTask listenerNeg) {
		ContextThemeWrapper ctw = new ContextThemeWrapper(dialogContext, R.style.DefaultTheme );
        AlertDialog.Builder builder = new AlertDialog.Builder( ctw );
		if (title != null)
			builder.setTitle(title);
		/*builder.setMessage(dialogMessage);
		builder.setPositiveButton("OK", listenerPos);
		if (listenerNeg == null){
			builder.setNegativeButton("Cancel", new OnClickListener(){

				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}});
		}
		else{
			builder.setNegativeButton("Cancel", listenerNeg);
		}*/
		
		View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout_two_options, null);
		TextView dialogMessageTextView = (TextView) dialogView.findViewById(R.id.twooptionsdialog_message);
		dialogMessageTextView.setText(dialogMessage);
		// first option button
		Button optionOneButton = (Button) dialogView.findViewById(R.id.twooptionsdialog_option1_button);
		// second option button
		Button optionTwoButton = (Button) dialogView.findViewById(R.id.twooptionsdialog_option2_button);
		builder.setView(dialogView);
		
		final AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		optionOneButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(listenerPos != null){
					Object result = listenerPos.onTaskInProgress();
					listenerPos.onTaskComplete(result);
				}
				dialog.dismiss();
			}
		});
		optionTwoButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(listenerNeg != null){
					Object result = listenerNeg.onTaskInProgress();
					listenerPos.onTaskComplete(result);
					
				}
				dialog.dismiss();
			}
		});
		
		return dialog;
	}
}