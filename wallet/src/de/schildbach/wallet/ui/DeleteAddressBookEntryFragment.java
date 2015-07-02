package de.schildbach.wallet.ui;

/**
 * Created by yezune on 15. 6. 19..
 */
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.CoinDefinition;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Wallet;

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.schildbach.wallet.AddressBookProvider;
import de.schildbach.wallet.Configuration;
import de.schildbach.wallet.Constants;
import de.schildbach.wallet.WalletApplication;
import de.schildbach.wallet.util.WalletUtils;
import unpaybank.unpay.wallet.R;

/**
 * @author Andreas Schildbach
 */
public final class DeleteAddressBookEntryFragment extends DialogFragment
{
    private static final String FRAGMENT_TAG = DeleteAddressBookEntryFragment.class.getName();

    private static final String KEY_ADDRESS = "address";
    private static final String KEY_SUGGESTED_ADDRESS_LABEL = "suggested_address_label";


    public static void delete(final FragmentManager fm, @Nonnull final String address)
    {
        delete(fm, address, null);
    }

    public static void delete(final FragmentManager fm, @Nonnull final String address, @Nullable final String suggestedAddressLabel)
    {
        final DialogFragment newFragment = DeleteAddressBookEntryFragment.instance(address, suggestedAddressLabel);
        newFragment.show(fm, FRAGMENT_TAG);
    }

    private static DeleteAddressBookEntryFragment instance(@Nonnull final String address, @Nullable final String suggestedAddressLabel)
    {
        final DeleteAddressBookEntryFragment fragment = new DeleteAddressBookEntryFragment();

        final Bundle args = new Bundle();
        args.putString(KEY_ADDRESS, address);
        args.putString(KEY_SUGGESTED_ADDRESS_LABEL, suggestedAddressLabel);
        fragment.setArguments(args);

        return fragment;
    }

    private AddressBookActivity activity;
    private WalletApplication application;
    private Configuration config;
    private Wallet wallet;
    private ContentResolver contentResolver;


    @Override
    public void onAttach(final Activity activity)
    {
        super.onAttach(activity);

        this.activity = (AddressBookActivity) activity;
        this.application = (WalletApplication) activity.getApplication();
        this.config = application.getConfiguration();
        this.wallet = application.getWallet();
        this.contentResolver = activity.getContentResolver();

    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        final Bundle args = getArguments();
        final String address = args.getString(KEY_ADDRESS);
        final String suggestedAddressLabel = args.getString(KEY_SUGGESTED_ADDRESS_LABEL);

        final LayoutInflater inflater = LayoutInflater.from(activity);

        final Uri uri = AddressBookProvider.contentUri(activity.getPackageName()).buildUpon().appendPath(address).build();

        final String label = AddressBookProvider.resolveLabel(activity, address);

        final DialogBuilder dialog = new DialogBuilder(activity);

        dialog.setTitle(R.string.delete_address_book_entry_dialog_title_delete);

        final View view = inflater.inflate(R.layout.delete_address_book_entry_dialog, null);

        final TextView viewAddress = (TextView) view.findViewById(R.id.delete_address_book_entry_address);
        viewAddress.setText(WalletUtils.formatHash(address, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE));

        final TextView viewLabel = (TextView) view.findViewById(R.id.delete_address_book_entry_label);
        viewLabel.setText(label != null ? label : suggestedAddressLabel);

        dialog.setView(view);

        final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int which)
            {
                if (which == DialogInterface.BUTTON_POSITIVE)
                {
                    //final String newLabel = viewLabel.getText().toString().trim();
                    //contentResolver.delete(uri, null, null);
                }
                else if (which == DialogInterface.BUTTON_NEUTRAL)
                {

                    try {

                        Address addr = new Address(wallet.getParams(), address);

                        if( wallet.getKeys().size() != 1 && wallet.getBalance(addr) == BigInteger.ZERO) {

                            ECKey key = wallet.findKeyFromPubHash(addr.getHash160());
                            wallet.removeKey(key);

                            contentResolver.delete(uri, null, null);
                            activity.updateAddressFragment();

                        }else {

                            Toast.makeText(application.getApplicationContext(),R.string.delete_address_book_entry_dialog_toast_cannot_delete, Toast.LENGTH_SHORT).show();
                        }

                    } catch (AddressFormatException e) {
                        System.err.println(address + " does not parse as a "+ CoinDefinition.coinName +" address of the right network parameters.");
                        return;
                    }

                }

                dismiss();
            }
        };

        dialog.setNeutralButton(R.string.button_delete, onClickListener);
        dialog.setNegativeButton(R.string.button_cancel, onClickListener);

        return dialog.create();
    }
}
