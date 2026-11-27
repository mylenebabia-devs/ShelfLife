package com.example.shelflife;
import java.time.LocalDate;
/** Dependency-free checks against the production Rules and Passwords classes. */
public final class DomainChecks {
 static int checks;
 static void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
 static void reject(Runnable action,String name){try{action.run();throw new AssertionError("Accepted "+name);}catch(IllegalArgumentException expected){checks++;}}
 public static void main(String[] args){
  checks=0;LocalDate today=LocalDate.now();int[] offsets={-1,0,1,3,7,8};String[] statuses={"Expired","Expires today","Expiring soon","Expiring soon","Expiring soon","Within date"};
  for(int i=0;i<offsets.length;i++)check(Rules.status(today.plusDays(offsets[i]).toEpochDay(),"Expiry",today).equals(statuses[i]),"date "+offsets[i]);
  check(Rules.status(null,"Unknown",today).contains("unknown"),"unknown date");
  check(Rules.status(today.minusDays(1).toEpochDay(),"Best Before",today).equals("Past best-before"),"best before distinct");
  check(Rules.status(today.toEpochDay(),"Best Before",today).equals("Best before today"),"best before today");
  check(Rules.status(today.plusYears(6).toEpochDay(),"Expiry",today).equals("Within date"),"no five year cutoff");
  for(LocalDate boundary:new LocalDate[]{LocalDate.of(2027,1,1),LocalDate.of(2028,3,1),LocalDate.of(2026,11,1)})check(Rules.status(boundary.minusDays(1).toEpochDay(),"Expiry",boundary).equals("Expired"),"calendar boundary");
  for(int n:new int[]{0,1,3,7})check(Rules.milestone(today.plusDays(n).toEpochDay(),today)==n,"milestone "+n);
  for(int n:new int[]{-1,2,8})check(Rules.milestone(today.plusDays(n).toEpochDay(),today)==-1,"nonmilestone "+n);
  check(Rules.milestone(null,today)==-1,"unknown milestone");
  check(Rules.email("staff@example.com"),"valid email");check(!Rules.email("staff@"),"invalid email");check(!Rules.email(" @example.com"),"whitespace email");
  check(Rules.normalizeEmail(" STAFF@Example.com ").equals("staff@example.com"),"email canonicalization");
  for(String q:new String[]{"0","-1","NaN","Infinity","abc"," ","0.0001","1000000001"})reject(()->Rules.quantity(q,false),"quantity "+q);
  check(Rules.quantity("2.125",false)==2.125,"fractional units");check(Rules.quantity("0",true)==0,"zero threshold");check(Rules.rounded(0.3-0.1)==0.2,"decimal rounding");
  reject(()->Rules.stockOut(6,5,null,"Unknown","Sold",today),"overdraw");reject(()->Rules.stockOut(1,5,today.minusDays(1).toEpochDay(),"Expiry","Sold",today),"expired sale");
  Rules.stockOut(5,5,today.toEpochDay(),"Expiry","Sold",today);check(true,"exact stock removal on expiry day");
  Rules.stockOut(1,5,today.minusDays(1).toEpochDay(),"Expiry","Expired",today);check(true,"expired waste removal");
  String salt=Passwords.salt(),hash=Passwords.hash("ExamplePass123!",salt);check(!hash.equals("ExamplePass123!"),"not plaintext");check(Passwords.verify("ExamplePass123!",salt,hash),"correct password");check(!Passwords.verify("wrong",salt,hash),"incorrect password");check(!Passwords.salt().equals(salt),"random salt");
  check(Csv.text("=SUM(A1)").startsWith("\"'"),"CSV formula neutralized");
  check(Csv.text("He said \"hi\"").equals("\"He said \"\"hi\"\"\""),"CSV quote escaping");
  check(Csv.text(null).equals("\"\""),"CSV null field");
  System.out.println("PASSED: "+checks+" production domain/auth checks");
 }
}
