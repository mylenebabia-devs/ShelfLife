package com.example.shelflife;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ListAdapter;
import com.google.android.material.card.MaterialCardView;
public class ProductAdapter extends ListAdapter<Product,ProductAdapter.VH> {
 public interface Listener {void onDetails(Product p);void onAddStock(Product p);}
 private final Listener listener;
 public ProductAdapter(Listener l){super(new DiffUtil.ItemCallback<Product>(){public boolean areItemsTheSame(@NonNull Product a,@NonNull Product b){return a.id==b.id;}public boolean areContentsTheSame(@NonNull Product a,@NonNull Product b){return false;}});listener=l;}
 @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int type){return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_product,p,false));}
 @Override public void onBindViewHolder(@NonNull VH h,int pos){Product p=getItem(pos);h.card.setCardBackgroundColor(0xffffffff);ProductImages.show(h.icon,p.photoPath,categoryIcon(p.category),false);android.graphics.drawable.GradientDrawable badge=new android.graphics.drawable.GradientDrawable();badge.setCornerRadius(24);badge.setColor(p.archived||p.expiryDate==null?0xffF0F2EE:Expiry.daysLeft(p)<0?0xffFFF0ED:Expiry.daysLeft(p)<=7?0xffFFF3D6:0xffECF4E8);h.status.setBackground(badge);h.stripe.setBackgroundColor(Expiry.color(p));h.name.setText(p.name);h.meta.setText(p.category+" · Batch "+p.stockReference);h.qty.setText(Rules.qty(p.quantity)+" "+p.unit+" · "+p.dateType+": "+Expiry.date(p));h.status.setText(p.deleted?"Deleted":p.archived?"Archived":p.quantity==0?"Depleted · "+Expiry.label(p):Expiry.label(p));h.status.setTextColor(Expiry.color(p));h.edit.setOnClickListener(v->listener.onDetails(p));h.add.setVisibility(p.archived?View.GONE:View.VISIBLE);h.add.setOnClickListener(v->listener.onAddStock(p));h.card.setOnClickListener(v->listener.onDetails(p));}
 static int categoryIcon(String category){if(category==null)return R.drawable.ic_box;switch(category){case "Dairy and Eggs":return R.drawable.ic_milk;case "Canned Goods":return R.drawable.ic_can;case "Bread and Bakery":return R.drawable.ic_bread;case "Beverages":return R.drawable.ic_bottle;case "Snacks and Biscuits":return R.drawable.ic_snack;case "Fruits and Vegetables":return R.drawable.ic_leaf;case "Frozen Foods":return R.drawable.ic_snow;default:return R.drawable.ic_box;}}
 static class VH extends RecyclerView.ViewHolder {MaterialCardView card;ImageView icon;View stripe;TextView name,meta,qty,status;Button edit,add;VH(View v){super(v);card=v.findViewById(R.id.card);icon=v.findViewById(R.id.productIcon);stripe=v.findViewById(R.id.viewStatus);name=v.findViewById(R.id.tvName);meta=v.findViewById(R.id.tvMeta);qty=v.findViewById(R.id.tvQty);status=v.findViewById(R.id.tvStatus);edit=v.findViewById(R.id.btnEdit);add=v.findViewById(R.id.btnDelete);}}
}
