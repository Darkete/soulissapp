package it.angelic.soulissclient.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissDataService;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.util.FontAwesomeEnum;
import it.angelic.soulissclient.util.FontAwesomeUtil;
import it.angelic.soulissclient.util.SoulissUtils;

public class TypicalsListAdapter extends BaseAdapter {
    Intent parentIntent;
    // private SoulissDBHelper dataSource;
    SoulissDataService mBoundService;
    private LayoutInflater mInflater;
    private Activity context;
    private SoulissTypical[] tipici;
    private SoulissPreferenceHelper opzioni;

    public TypicalsListAdapter(Activity context, SoulissDataService serv, SoulissTypical[] versio, Intent forExtra,
                               SoulissPreferenceHelper op) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.tipici = versio;

		/*for (int i = 0; i < versio.length; i++) {
			if (versio[i].getCtx() == null)
				throw new RuntimeException("Non dovrebbe essere nullo, controlla il giro");
		}*/

        opzioni = op;
        parentIntent = forExtra;
        // dataSource = ds;
        mBoundService = serv;
    }

    public SoulissDataService getmBoundService() {
        return mBoundService;
    }

    public void setmBoundService(SoulissDataService mBoundService2) {
        mBoundService = mBoundService2;
    }

    public int getCount() {
        // Hack lista vuota
        if (tipici == null || tipici.length == 0)
            return 1;
        return tipici.length;
    }

    public Object getItem(int position) {
        return tipici[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public SoulissTypical[] getTypicals() {
        return tipici;
    }

    public void setTypicals(SoulissTypical[] in) {
        tipici = in;
    }

    public View getView(int position, View convertView, final ViewGroup parent) {
        TypicalViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listview_typical, parent, false);
            final View ccopy = convertView;
            holder = new TypicalViewHolder();
            holder.expand = (ImageView) convertView.findViewById(R.id.imageButtonExpand);
            holder.textslot = (TextView) convertView.findViewById(R.id.TextViewSlot);
            holder.textUpdated = (TextView) convertView.findViewById(R.id.TextViewUpdated);
            holder.textStatus = (TextView) convertView.findViewById(R.id.textViewStatus);
            holder.textStatusVal = (TextView) convertView.findViewById(R.id.textViewStatusVal);
            holder.image = (TextView) convertView.findViewById(R.id.node_icon);
            holder.imageFav = (ImageView) convertView.findViewById(R.id.imageButtonFav);
            holder.imageTag = (ImageView) convertView.findViewById(R.id.imageButtonTag);

            holder.linearActionsLayout = (LinearLayout) convertView.findViewById(R.id.linearLayoutButtons);
            // linButton.removeAllViews();
            convertView.setTag(holder);
            holder.expand.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ccopy.performLongClick();

                }
            });
        } else {
            holder = (TypicalViewHolder) convertView.getTag();
        }
        //fuori da if-else per evitare #74
        if (tipici.length > 0) // magari e` vuoto
            holder.data = tipici[position];


        if (opzioni.isLightThemeSelected()) {

            holder.textslot.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.textUpdated.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.expand.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.overflow_dots));
            holder.expand.setColorFilter(R.color.black);
        }
		/* Nodo vuota */
        if (tipici.length == 0) {

            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, FontAwesomeEnum.fa_exclamation_triangle.getFontName());
            //holder.image.setImageResource(android.R.drawable.ic_dialog_alert);
            //holder.image.setColorFilter(ContextCompat.getColor(context, R.color.aa_yellow),
            //        android.graphics.PorterDuff.Mode.SRC_ATOP);
            holder.textslot.setText(context.getResources().getString(R.string.node_empty));
            holder.textStatus.setText(context.getResources().getString(R.string.node_empty_desc));
            // holder.evidenza.setBackgroundColor(context.getResources().getColor(color.trans_black));
            return convertView;
        }
		/* INFO slot e Alias Name */
        holder.textslot.setText(tipici[position].getNiceName());
        holder.textUpdated.setText(context.getString(R.string.update) + " "
                + SoulissUtils.getTimeAgo(tipici[position].getTypicalDTO().getRefreshedAt()) + " - "
                + context.getString(R.string.manual_slot) + ": " + tipici[position].getSlot());
        holder.textStatus.setText(context.getResources().getString(R.string.typical).toUpperCase(Locale.getDefault())
                + ": " + tipici[position].getTypicalDTO().getTypicalDec() + " - "
                + context.getResources().getString(R.string.status));
		/* Icona del nodo */
        if (tipici[position].getIconResourceId() != 0) {
            FontAwesomeUtil.prepareFontAweTextView(context, holder.image, tipici[position].getIconResourceId());
        }
        //Preferito, TagId == 0
        if (tipici[position].getTypicalDTO().isFavourite()) {
            holder.imageFav.setVisibility(View.VISIBLE);
        }else{
            holder.imageFav.setVisibility(View.GONE);
        }
        //puo essere ANCHE tagged
        if (tipici[position].getTypicalDTO().isTagged()) {
            holder.imageTag.setVisibility(View.VISIBLE);
        } else {
            holder.imageTag.setVisibility(View.GONE);
        }

        tipici[position].setOutputDescView(holder.textStatusVal);
        holder.linearActionsLayout.removeAllViews();

        // richiama l'overloaded del tipico relativo
        tipici[position].getActionsLayout(context, holder.linearActionsLayout);

        // linearActionsLayout.setVisibility(View.VISIBLE);

        if (opzioni.getTextFx()) {
            Animation a = AnimationUtils.loadAnimation(context, R.anim.scalerotale);
            a.reset();
            a.setStartOffset(100 * position);
            // Animazione testo sotto holder.text.clearAnimation();
            holder.image.startAnimation(a);
        }
        return convertView;
    }

    public static class TypicalViewHolder {
        public ImageView expand;
        public SoulissTypical data;
        View shader;
        TextView textStatus;
        TextView textStatusVal;
        TextView textslot;
        TextView textUpdated;
        LinearLayout linearActionsLayout;
        TextView image;
        ImageView imageFav;
        ImageView imageTag;
    }

}