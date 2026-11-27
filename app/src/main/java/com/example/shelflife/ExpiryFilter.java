package com.example.shelflife;

public enum ExpiryFilter {
 ALL("All stock"),ATTENTION("Needs attention"),SOON("Expiring soon"),TODAY("Today"),EXPIRED("Expired"),BEST("Best before"),UNKNOWN("Unknown date"),LOW("Low stock"),ARCHIVED("Archived"),DELETED("Deleted");
 public final String title;ExpiryFilter(String t){title=t;}
 public boolean matches(Product p){long d=Expiry.daysLeft(p);switch(this){case ATTENTION:return p.quantity>0&&(p.expiryDate==null||d<=7);case SOON:return p.quantity>0&&"Expiry".equals(p.dateType)&&d>=1&&d<=7;case TODAY:return p.quantity>0&&d==0;case EXPIRED:return p.quantity>0&&"Expiry".equals(p.dateType)&&d<0;case BEST:return p.quantity>0&&"Best Before".equals(p.dateType);case UNKNOWN:return p.quantity>0&&p.expiryDate==null;default:return true;}}
}
