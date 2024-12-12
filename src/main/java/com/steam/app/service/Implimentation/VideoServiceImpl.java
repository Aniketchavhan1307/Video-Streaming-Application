package com.steam.app.service.Implimentation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.steam.app.entities.Video;
import com.steam.app.repositories.VideoRepository;
import com.steam.app.service.VideoService;

import jakarta.annotation.PostConstruct;



@Service
public class VideoServiceImpl implements VideoService
{
	@Autowired
	private VideoRepository videoRepo;
	
	@Value("${file.video.hsl}")
	String HSL_DIR ;
	
	@Value("${files.video}")			// fetching the folder name from application.properfile 
	String DIR;

	@PostConstruct
	public void init() 
	{
		File file = new File(DIR);
		
		// this is for segment -----this is another way of create directories .....so here we saw 2 ways....
		
		try
		{
			Files.createDirectories(Paths.get(HSL_DIR));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		
		// This is for video folder
		
		if (!file.exists()) 			// Check if the folder does not exist
		{ 
			if (file.mkdir())				// Create the directory
			{ 
				System.out.println("Folder created: " + file.getAbsolutePath());
			} 
			else 
			{
				System.err.println("Failed to create folder: " + DIR);
			}
		} 
		else 
		{
			System.out.println("Folder already exists: " + file.getAbsolutePath());
		}
	}

	
	
	public Video save(Video video, MultipartFile file) 
	{
		try {
		// original file name
		String filename = file.getOriginalFilename();
		
		String contentType = file.getContentType();
		
		InputStream inputStream = file.getInputStream();
		
		
		// folder path : create 
		// file path
		String cleanFileName = StringUtils.cleanPath(filename);
		
		// folder path
		String cleanFolder = StringUtils.cleanPath(DIR);
		
		// folder path with file name
		Path path = Paths.get(cleanFolder, cleanFileName);
		
		System.out.println(path);
		System.err.println(contentType);
		
		
		// copy file to the folder
		Files.copy(inputStream,  path, StandardCopyOption.REPLACE_EXISTING);
		
		// video meta data 
		
		video.setContentType(contentType);
		video.setFilePath(path.toString());
				
		// save video first in database
		Video savedVideo = videoRepo.save(video);
		
		
		// Processing Video........
		processVideo(savedVideo.getVideoId());
		
		// Delete Actual Video file and database entry delete if exception occur while saving or after processVideo in segments..
		
		
		// meta data save in database
		
		return savedVideo ;
		
		
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			 throw new RuntimeException("Failed to save video file", e);
		}
		
	}

	
	
	public Video get(String videoId) 
	{
		Video video = videoRepo.findById(videoId).orElseThrow(()-> new RuntimeException("Video not found.."));
		 return video;
	}

	
	public List<Video> getAll() 
	{
		return videoRepo.findAll();
	}
	
	

	public Video getByTitle(String title) 
	{
		return null;
	}
	
	
	
	
	
	// ============================================================================
	//The process described in the code is video transcoding and packaging for HTTP Live Streaming (HLS).
	// This process is common in building video streaming platforms like YouTube or Netflix, where videos are optimized for playback on different devices and network conditions.
	//
	
	   public String processVideo(String videoId) {

	        Video video = this.get(videoId);
	        String filePath = video.getFilePath();

	        //path where to store data:
	        Path videoPath = Paths.get(filePath);


//	        String output360p = HSL_DIR + videoId + "/360p/";
//	        String output720p = HSL_DIR + videoId + "/720p/";
//	        String output1080p = HSL_DIR + videoId + "/1080p/";

	        try {
//	            Files.createDirectories(Paths.get(output360p));
//	            Files.createDirectories(Paths.get(output720p));
//	            Files.createDirectories(Paths.get(output1080p));

	            // ffmpeg command
	            Path outputPath = Paths.get(HSL_DIR, videoId);

	            Files.createDirectories(outputPath);


	            // FFmpeg command
	            String ffmpegCmd = String.format(
	                "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%03d.ts\" \"%s/master.m3u8\"",
	                videoPath, outputPath, outputPath
	            );
	            
	            
//	            StringBuilder ffmpegCmd = new StringBuilder();
//	            ffmpegCmd.append("ffmpeg  -i ")
//	                    .append(videoPath.toString())
//	                    .append(" -c:v libx264 -c:a aac")
//	                    .append(" ")
//	                    .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
//	                    .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
//	                    .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
//	                    .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
//	                    .append("-master_pl_name ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
//	                    .append("-f hls -hls_time 10 -hls_list_size 0 ")
//	                    .append("-hls_segment_filename \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
//	                    .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");


	            System.err.println(ffmpegCmd);
	            //file this command
	         // Execute the FFmpeg command
	            ProcessBuilder processBuilder = new ProcessBuilder(
	                "ffmpeg",
	                "-i", videoPath.toString(),
	                "-c:v", "libx264",
	                "-c:a", "aac",
	                "-strict", "-2",
	                "-f", "hls",
	                "-hls_time", "10",
	                "-hls_list_size", "0",
	                "-hls_segment_filename", outputPath.resolve("segment_%03d.ts").toString(),
	                outputPath.resolve("master.m3u8").toString()
	            );

	            processBuilder.inheritIO();
	            Process process = processBuilder.start();
	            int exit = process.waitFor();
	            if (exit != 0) {
	                throw new RuntimeException("video processing failed!!  "+ exit);
	            }

	            return videoId;


	        } catch (IOException ex) {
	            throw new RuntimeException("Video processing fail!! ---> "+ ex.getMessage(), ex);
	        } catch (InterruptedException e) {
	            throw new RuntimeException(e);
	        }


	    }
}
	
	
	
	
	
	
	
//	
//	
//
//
//
//	@Override
//	public String processVideo(String videoId, MultipartFile file) 
//	{
//		Video video = this.get(videoId);
//		String filePath = video.getFilePath();
//		
//		// path where to store data:
//		Path videoPath = Paths.get(filePath);
//		
//		
//		String output360p = HSL_DIR + videoId + "/360p/";
//		String output720p = HSL_DIR + videoId + "/720p/";
//		String output1080p = HSL_DIR + videoId + "/1080p/";
//		
//		
//		try 
//		{
//			Files.createDirectories(Paths.get(output360p));
//			Files.createDirectories(Paths.get(output720p));
//			Files.createDirectories(Paths.get(output1080p));
//			
//			// ffmpeg command..
//			
//			String ffmpegCmd = String.format(
//					"ffmpeg -i \"%s\" -c",videoPath, HSL_DIR
//					);
//			
//		//	StringBuilder ffmpegCmd = new StringBuilder();
////			ffmpegCmd.append("ffmpeg -i")
////						.append(videoPath.toString())
////						.append(" ");
//		} 
//		catch (IOException e)
//		{
//			e.printStackTrace();
//			throw new RuntimeException("Video processing failed....");
//		}
//		
//		
//		return null;
//	}
//  
//}
