package com.example.shelflife;

import androidx.room.*;
@Entity(tableName="grocery_staff",indices=@Index(value="email",unique=true))
public class Staff {
 @PrimaryKey(autoGenerate=true) public long id;
 public String name,email,salt,passwordHash;
 public String phone,photoPath;
}
