package com.example.shelflife;

import android.app.Application;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import java.util.*;
public class ShelfViewModel extends AndroidViewModel {
 private final LiveData<List<Product>> all;private final MediatorLiveData<List<Product>> visible=new MediatorLiveData<>();
 private String query="",category="All";private ExpiryFilter filter=ExpiryFilter.ALL;private boolean byName;private int generation;
 private final Handler main=new Handler(Looper.getMainLooper());
 public ShelfViewModel(@NonNull Application app){super(app);all=AppDatabase.get(app).productDao().observeAll();visible.addSource(all,x->refresh());}
 public LiveData<List<Product>> getAll(){return all;}public LiveData<List<Product>> getVisible(){return visible;}
 public String query(){return query;}public String category(){return category;}public ExpiryFilter filter(){return filter;}
 public void setQuery(String s){query=s;refresh();}public void setCategory(String s){if(category.equals(s))return;category=s;refresh();}public void setFilter(ExpiryFilter f){filter=f;refresh();}public void sortName(boolean b){byName=b;refresh();}
 public void refresh(){List<Product> current=all.getValue();if(current==null)return;List<Product>source=new ArrayList<>(current);final String q=query.trim().toLowerCase(Locale.ROOT),c=category;final ExpiryFilter f=filter;final boolean sort=byName;final int token=++generation;
 Auth.IO.execute(()->{Map<String,Double> totals=new HashMap<>();for(Product p:source)if(!p.archived)totals.put(p.sku,totals.getOrDefault(p.sku,0.0)+p.quantity);List<Product> out=new ArrayList<>();for(Product p:source){if(f==ExpiryFilter.DELETED){if(!p.deleted)continue;}else if(f==ExpiryFilter.ARCHIVED){if(!p.archived||p.deleted)continue;}else if(p.archived||p.deleted)continue;String text=p.name+" "+p.sku+" "+p.batchNo+" "+Objects.toString(p.barcode,"");if(!text.toLowerCase(Locale.ROOT).contains(q)||(!c.equals("All")&&!CategorySuggestions.key(c).equals(CategorySuggestions.key(p.category)))||!f.matches(p))continue;if(f==ExpiryFilter.LOW&&Rules.rounded(totals.getOrDefault(p.sku,0.0))>p.lowStock)continue;out.add(p);}if(sort)out.sort(Comparator.comparing(p->p.name.toLowerCase(Locale.ROOT)));main.post(()->{if(token==generation)visible.setValue(out);});});}
}
