package com.steam.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.steam.app.entities.Video;

public interface VideoService 
{
	// SAVE Video
	
	Video save( Video video, MultipartFile file);
	
	
	// get video by id
	
	Video get(String videoId);
	
	// get all video
	List<Video> getAll();
	
	// get video by title
	
	Video getByTitle(String title);
	
	// video processing
	
	String processVideo(String videoId);
}
