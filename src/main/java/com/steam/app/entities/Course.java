package com.steam.app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "yt_courses")
@Data
@NoArgsConstructor
public class Course
{

	@Id
	    private  String id;

	    private  String title;

//	    @OneToMany(mappedBy = "course")
//	    private List<Video> list=new ArrayList<>();
	    
}
