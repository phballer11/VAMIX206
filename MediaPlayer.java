package vamixA3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class MediaPlayer implements ActionListener{
	
	//initialize components
	//Media 
    private final EmbeddedMediaPlayerComponent _mediaPlayerComponent;
    private  final EmbeddedMediaPlayer _video;
    private String _filePath;
    private String _fileName;
    private JPanel _panel = new JPanel();
    
    //Basic Operations
    private JFileChooser _fc = new JFileChooser();
    private JButton _mute = new JButton("mute");
    private JButton _forward = new JButton(">>");
    private JButton _backward = new JButton("<<");
    private JButton _playPause = new JButton("||");
    private Timer _ticker = new Timer(900,this);
    private JLabel _timePlayed = new JLabel();
    
    //Edit Options
    private JButton _extractA = new JButton("Extract Audio");
    private JButton _extractV = new JButton("Extract Video");
    private JButton _export = new JButton("Export");
    private JButton _save = new JButton("Save Change");
    private JButton _replaceBtn = new JButton("Replace Audio");
    private JButton _mergeBtn = new JButton("Merge Audio");
    private JButton _undoBtn = new JButton("Undo");
    private boolean _isSave = false;
    
    //Color Options
    private JButton _colorBtn = new JButton("Color");
    private String _rgb;
    
    //Font Options
    private JButton _fontBtn = new JButton("Font");
    private JDialog _fontWindow = new JDialog();
    private ArrayList<String> _fontList = new ArrayList<String>();
    private JButton _fontSize = new JButton("FontSize:16"); //default size
	private JButton _Mono = new JButton("Mono");
	private JButton _Sans = new JButton("Sans");
	private JButton _Serif = new JButton("Serif");
	private JButton _Italic = new JButton("ITALIC");
	private JButton _Bold = new JButton("BOLD");
	private JLabel _font = new JLabel();
	private Boolean _isMono = false;
	private Boolean _isSans = true; //default font
	private Boolean _isSerif = false;
	private Boolean _isBold = false;
	private Boolean _isItalic = false;
	
	//Input Text
	private JButton _textBtn = new JButton("AddText");
	
	//Time Options
	private JButton _startTimeBtn = new JButton("StartTimeTo");
	private JButton _endTimeBtn = new JButton("ToEndTime");

	//edit Option 0=font file 1=text 2=fontsize 3=font color 4= start to this time 5 = this time to end
	//private ArrayList<String> _cmdList = new ArrayList<String>();
	private String[] _cmdList = new String[6];
	private JButton _preview = new JButton("Preview");
	
	//extract window
	protected static JDialog _waitWin = new JDialog();
	protected static JLabel _waitLabel= new JLabel("<html>Please wait and not touch any buttons!</html>");
	protected static JLabel _doneLabel = new JLabel("File Exported!");
	
	//Command History
	File _file;
	private String _cmd;
	private ArrayList<String> _cmdHist = new ArrayList<String>();
	
   
    
    //bash command process and swing worker
	private Process _process;
	private SwingExtract _extractTask = new SwingExtract();
	

    //constructor for class
    protected MediaPlayer(String args) {
    	
    	//Get media file name and full path
    	_filePath = args;
    	String[] tmp = _filePath.split("/");
		_fileName = tmp[tmp.length-1];
		
		//Create frame and set title
        JFrame frame = new JFrame("206VAMIX: "+_fileName);
        
        JPanel editPanel = new JPanel();
    
        
        //construct media player
        _mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        _video = _mediaPlayerComponent.getMediaPlayer();
        
        
        //set LAF
        FlowLayout flow = new FlowLayout();
        flow.setVgap(5);
        _panel.setLayout(new BorderLayout());
        editPanel.setLayout(flow);
        editPanel.setBackground(Color.gray);
        editPanel.setPreferredSize(new Dimension(150,300));
        
        //set extractWin
        _waitWin.setLayout(null);
        _waitWin.setMinimumSize(new Dimension(230,100));
        _waitWin.setLocationRelativeTo(null);
        _waitLabel.setFont(new Font("Arial",Font.BOLD,16));
        _doneLabel.setFont(new Font("Arial",Font.BOLD,20));
        _waitLabel.setBounds(20, 10, 200, 50);
        _doneLabel.setBounds(35, 10, 200, 50);

        _waitWin.add(_waitLabel);
        _waitWin.add(_doneLabel);
        _doneLabel.setVisible(false);
        
        //initialize available fonts and window
        _font.setFont(new Font("Sans",Font.PLAIN,20));
        _font.setText("Sans");
        this.createFont();
    	this.setButtonSize(_Mono);
    	this.setButtonSize(_Sans);
    	this.setButtonSize(_Serif);
    	this.setButtonSize(_Italic);
    	this.setButtonSize(_Bold);
    	this.setButtonSize(_undoBtn);
    
       
        //Set Button size
    	this.setButtonSize(_save);
    	this.setButtonSize(_preview);
    	this.setButtonSize(_textBtn);
    	this.setButtonSize(_fontSize);
        this.setButtonSize(_extractA);
        this.setButtonSize(_extractV);
        this.setButtonSize(_mute);
        this.setButtonSize(_extractA);
        this.setButtonSize(_export);
        this.setButtonSize(_replaceBtn);
        this.setButtonSize(_mergeBtn);
        this.setButtonSize(_fontBtn);
        this.setButtonSize(_colorBtn);
        this.setButtonSize(_startTimeBtn);
        this.setButtonSize(_endTimeBtn);
        _font.setPreferredSize(new Dimension(130,30));
        _forward.setPreferredSize(new Dimension(30,30));
        _backward.setPreferredSize(new Dimension(30,30));
        _playPause.setPreferredSize(new Dimension(30,30));
       

        //Position to add Text (x,y)
        _cmdList[0] = _fontList.get(4);
        _cmdList[2] = "16";
        _cmdList[3] = "0xffffff";
        //set time display font
        _timePlayed.setFont(new Font("Arial",Font.BOLD,14));
        _timePlayed.setForeground(Color.white);
        _timePlayed.setText("00:00");
       
        //Add ActionListener
        _undoBtn.addActionListener(this);
        _startTimeBtn.addActionListener(this);
        _endTimeBtn.addActionListener(this);
        _export.addActionListener(this);
        _preview.addActionListener(this);
        _textBtn.addActionListener(this);
        _fontSize.addActionListener(this);
        _Mono.addActionListener(this);
        _Sans.addActionListener(this);
        _Serif.addActionListener(this);
        _Bold.addActionListener(this);
        _Italic.addActionListener(this);
        _colorBtn.addActionListener(this);
        _fontBtn.addActionListener(this);
        _replaceBtn.addActionListener(this);
        _mute.addActionListener(this);
        _forward.addActionListener(this);
        _backward.addActionListener(this);
        _playPause.addActionListener(this);
        _extractA.addActionListener(this);
        _extractV.addActionListener(this);
        _mergeBtn.addActionListener(this);
        _save.addActionListener(this);
        
        //add to edit panel,adding order relates to display order
        editPanel.add(_export);
        editPanel.add(_save);
        editPanel.add(_undoBtn);
        editPanel.add(_replaceBtn);
        editPanel.add(_mergeBtn);
        editPanel.add(_extractA);
        editPanel.add(_extractV);  
        editPanel.add(_textBtn);
        editPanel.add(_fontSize);
        editPanel.add(_fontBtn);  
        editPanel.add(_Bold);
        editPanel.add(_Italic);
        editPanel.add(_colorBtn);
        editPanel.add(_font);
        editPanel.add(_startTimeBtn);
        editPanel.add(_endTimeBtn);
        editPanel.add(_preview);
        editPanel.add(_backward);
        editPanel.add(_playPause);
        editPanel.add(_forward);
        editPanel.add(_mute);
        editPanel.add(_timePlayed);
      

        //Main panel
        _panel.add(_mediaPlayerComponent,BorderLayout.CENTER);
        _panel.add(editPanel,BorderLayout.WEST);


        //set JFrame to Main panel
        frame.setContentPane(_panel);
        
        //Set JFrame GUI
        frame.setLocation(100, 100);
        frame.setSize(1050, 800);
        frame.setDefaultCloseOperation(MenuA3.EXIT_ON_CLOSE);
        frame.setVisible(true);
        //get history
        try {
			this.getHistory();
		} catch (IOException e) {
			e.printStackTrace();
		}

        //play video and start timer
       
      	 _video.playMedia(_filePath);
      	 _ticker.start();                
 
    }

   //retain history of file if exist
   private void getHistory() throws IOException {
	    String[] fileName = _fileName.split("\\.");
	   	_file = new File("VAMIXHistory"+File.separator+fileName[0]+"History.txt");
	   	System.out.println(_file);
		if(_file.exists()){
			int userChoice = JOptionPane.showOptionDialog(null, "This file appears to be edited before!\nWould you like to Continue or treat as New?", "Surprise!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Continue","New"}, "default");
			//0 is continue 1 is new
			if(userChoice == 0){
				JOptionPane.showMessageDialog(null, "Please locate the original source video","Locate File",JOptionPane.INFORMATION_MESSAGE);
				_filePath = this.pick()[1];
				if(_filePath != null){
					BufferedReader br = new BufferedReader(new FileReader(_file));
					String line;
					while((line = br.readLine()) != null){
						_cmdHist.add(line);
					}
				}
				else{
					JOptionPane.showMessageDialog(null,"ERROR! You must locate the source video!\n Program exiting..","Error!",JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}		
		}
	}

//font list
    private void createFont(){
    	String path = "/usr/share/fonts/truetype/freefont/";
    	_fontList.add(path+"FreeMono.ttf");
    	_fontList.add(path+"FreeMonoBold.ttf");
    	_fontList.add(path+"FreeMonoOblique.ttf");
    	_fontList.add(path+"FreeMonoBoldOblique.ttf");
    	_fontList.add(path+"FreeSans.ttf");
    	_fontList.add(path+"FreeSansBold.ttf");
    	_fontList.add(path+"FreeSansOblique.ttf");
    	_fontList.add(path+"FreeSansBoldOblique.ttf");
    	_fontList.add(path+"FreeSerif.ttf");
    	_fontList.add(path+"FreeSerifBold.ttf");
    	_fontList.add(path+"FreeSerifItalic.ttf");
    	_fontList.add(path+"FreeSerifBoldItalic.ttf");	
    
    	FlowLayout flow = new FlowLayout();
    	flow.setHgap(200);
    	_fontWindow.setLayout(flow);
    	_fontWindow.setVisible(false);
    	_fontWindow.setMinimumSize(new Dimension(150,150));
    	_fontWindow.setLocationRelativeTo(null);
    	_fontWindow.add(_Mono);
    	_fontWindow.add(_Sans);
    	_fontWindow.add(_Serif);
    

    }

	//Get file browser 
	private String[] pick() throws FileNotFoundException{
			String[] fileInfo = new String[2];
			FileFilter videoAudioFilter = new FileNameExtensionFilter("Video/Audio files",new String[] {"mp3","wav","mp4","avi"} );
	
			_fc.setFileFilter(videoAudioFilter);
			
			if(_fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				
				File file = _fc.getSelectedFile();	
				fileInfo[0] = file.getName();
				fileInfo[1] = file.getPath();
			}
			else{
				System.out.println("No file");
			}
			return fileInfo;
	}

    //Set button size to the same
    private void setButtonSize(JButton e){
    	e.setPreferredSize(new Dimension(120,25));
    }
    
    //Execute bash command
	private String execCmd(String cmd){
		try {
		
			ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c",cmd);
			builder.redirectErrorStream(true);
		
			_process = builder.start();
			InputStream stdout = _process.getInputStream();
			BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
			String output = stdoutBuffered.readLine();
			String line = null;
			while ((line = stdoutBuffered.readLine()) != null ) {
				
				output = output + line;
			}
		
			return output;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cmd;
	}
	
	
	//Convert seconds to min:sec format
    private void timeConvert(int time){
    	if(time < 10){
    		_timePlayed.setText("00:0" + time);
    	}
    	else if(time > 9 && time < 60){
    		_timePlayed.setText("00:"+time);
    	}
    	else if(time >60){
    		
    		int minute = time/60;
    		int seconds = time%60;
    		if(minute <10){
    			if(seconds < 10){
    				_timePlayed.setText("0"+minute+":0"+seconds);
    			}
    			else{
    				_timePlayed.setText("0"+minute+":"+seconds);
    			}
    		}
    		else{
    			if(seconds < 10){
    				_timePlayed.setText(minute+":0"+seconds);
    			}
    			else{
    				_timePlayed.setText(minute+":"+seconds);
    			}	
    		}	
    	}
    }
    
  	
    //convert decimal to Hex
    public String convert(int n) {
    	  return Integer.toHexString(n);
    	}
	
    @Override
	public void actionPerformed(ActionEvent e) {
    	
		//mute button
		if(e.getSource() == _mute){
			if(_video.isMute()){
				_mute.setText("mute");
				_video.mute();

			}else{
				_mute.setText("unmute");
				_video.mute();
			}
		}
		//forward
		else if(e.getSource() == _forward){
			_video.skip(10000);
		}
		//backward
		else if(e.getSource() == _backward){
			_video.skip(-10000);
		}
		//show time
		else if(e.getSource() == _ticker){
			int time = (int)(_video.getTime() / 1000.0);
			this.timeConvert(time);
		}
		//play or pause video
		else if(e.getSource() == _playPause){
			if(_playPause.getText().equals("||")){
				_video.pause();
				_playPause.setText(">");
			}
			else if(_playPause.getText().equals(">")){
				_video.play();
				_playPause.setText("||");
			}
		}
		//extract audio
		else if(e.getSource() == _extractA){
			
			if(_video.getAudioTrackCount() != 0){
				String output = JOptionPane.showInputDialog(null,"Enter Output Audio file Name(No File Extension needed):");
				if(output != null){
					if(_video.isPlaying()){
						_video.pause();
						_playPause.setText(">");
					}
					String cmd = "avconv -i " + _filePath + " -vn -acodec copy " + output+".mp3";
					_doneLabel.setVisible(false);
					_waitWin.setVisible(true);
					_waitLabel.setVisible(true);

					_extractTask.setCmd(cmd);
					_extractTask.execute();
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "No Audio Track", "Error!",JOptionPane.INFORMATION_MESSAGE);
			}
		}
		//extract video
		else if(e.getSource() == _extractV){
			if(_video.getVideoTrackCount() != 0){
				String output = JOptionPane.showInputDialog(null,"Enter Output Video file Name(No File Extension needed):");
				if(output != null){
					if(_video.isPlaying()){
						_video.pause();
						_playPause.setText(">");
					}
					String cmd = "avconv -i " + _filePath + " -vn -acodec copy -map 0:v " + output+".mp4";
					
					_doneLabel.setVisible(false);
					_waitWin.setVisible(true);
					_waitLabel.setVisible(true);

					_extractTask.setCmd(cmd);
					_extractTask.execute();
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "No Video Track", "Error!",JOptionPane.INFORMATION_MESSAGE);

			}
		}
		//Replace audio with chosen file
		else if(e.getSource() == _replaceBtn){
			try {
				String audioPath = this.pick()[1];
				if(audioPath != null){
					String output = JOptionPane.showInputDialog(null,"Enter Output Video file Name(No File Extension needed):");
					if(output != null){
						if(_video.isPlaying()){
							_video.pause();
							_playPause.setText(">");
						}
						
						String cmd = "avconv -i "+_filePath + " -i "+audioPath + " -map 0:v -map 1:a -strict experimental "+output+".mp4";
						_doneLabel.setVisible(false);
						_waitWin.setVisible(true);
						_waitLabel.setVisible(true);

						_extractTask.setCmd(cmd);
						_extractTask.execute();
					}
				}
			} catch (FileNotFoundException e1) {
				
				e1.printStackTrace();
			}
		}
		/*
		 * Only works on UG4 labs
		 * not my laptop, not compsci lab level 1
		 */
		else if(e.getSource() == _mergeBtn){
			try {
				String audioPath = this.pick()[1];
				if(audioPath != null){
					String output = JOptionPane.showInputDialog(null,"Enter Output Video file Name(No File Extension needed):");
					if(output != null){
						if(_video.isPlaying()){
							_video.pause();
							_playPause.setText(">");
						}
						String cmd = "avconv -i "+_filePath + " -i "+audioPath + " -filter_complex amix=inputs=2 -strict experimental "+output+".mp4";
						_doneLabel.setVisible(false);
						_waitWin.setVisible(true);
						_waitLabel.setVisible(true);

						_extractTask.setCmd(cmd);
						
						_extractTask.execute();
					}
				}
			} catch (FileNotFoundException e1) {
				
				e1.printStackTrace();
			}
		}
		else if(e.getSource() == _fontBtn){
		
			_fontWindow.setVisible(true);
		}
		
		//get color chosen
		else if(e.getSource() == _colorBtn){
			
			
	        Color chosenColor = JColorChooser.showDialog(_colorBtn, "Choose Font Color", Color.black);
	        
	        if(chosenColor !=null){
	        	_font.setForeground(chosenColor);
	        	String r = this.convert(chosenColor.getRed());
	        	String g = this.convert(chosenColor.getGreen());
	        	String b = this.convert(chosenColor.getBlue());
	        	_rgb = "0x"+r+g+b;	
	        	_cmdList[3] = _rgb;
	        }
		}
		//get font choices
		else if(e.getSource() == _Mono){
			_fontWindow.setVisible(false);
			if(_isMono){
				_font.setText("Sans");
				_isMono = false;
				
				if(_isBold && _isItalic){
					_font.setFont(new Font("Sans",Font.BOLD | Font.ITALIC,20));
					_cmdList[0] = _fontList.get(7);
				}
				else if(_isBold){
					_font.setFont(new Font("Sans",Font.BOLD,20));
					_cmdList[0] = _fontList.get(5);

				}
				else if(_isItalic){
					_font.setFont(new Font("Sans",Font.ITALIC,20));
					_cmdList[0] = _fontList.get(6);

				}else {
					_font.setFont(new Font("Sans",Font.PLAIN,20));
					_cmdList[0] = _fontList.get(4);

				}
				
			
			}
			else{
				_font.setText("Mono");
				_isMono = true;
				if(_isBold && _isItalic){
					_font.setFont(new Font("Mono",Font.BOLD | Font.ITALIC,20));
					_cmdList[0] = _fontList.get(3);

				}
				else if(_isBold){
					_font.setFont(new Font("Mono",Font.BOLD,20));
					_cmdList[0] = _fontList.get(1);

				}
				else if(_isItalic){
					_font.setFont(new Font("Mono",Font.ITALIC,20));
					_cmdList[0] = _fontList.get(2);

				}else {
					_font.setFont(new Font("Mono",Font.PLAIN,20));
					_cmdList[0] = _fontList.get(0);

				}
				
			}
		}
		
		else if(e.getSource() == _Sans){
			_fontWindow.setVisible(false);
			
			if(_isSans){
				_isSans=false;
			}
			else{
				_isSans=true;
			}
			_font.setText("Sans");
			_isMono = false;

			if(_isBold && _isItalic){
				_font.setFont(new Font("Sans",Font.BOLD | Font.ITALIC,20));
				_cmdList[0] = _fontList.get(7);

			}
			else if(_isBold){
				_font.setFont(new Font("Sans",Font.BOLD,20));
				_cmdList[0] = _fontList.get(5);

			}
			else if(_isItalic){
				_font.setFont(new Font("Sans",Font.ITALIC,20));
				_cmdList[0] = _fontList.get(6);

			}else {
				_font.setFont(new Font("Sans",Font.PLAIN,20));
				_cmdList[0] = _fontList.get(4);

			}
			
		}
		else if(e.getSource() == _Serif){
			_fontWindow.setVisible(false);
			if(_isSerif){
				_font.setText("Sans");
				_isSerif = false;

				if(_isBold && _isItalic){
					_font.setFont(new Font("Sans",Font.BOLD | Font.ITALIC,20));
					_cmdList[0] = _fontList.get(7);

				}
				else if(_isBold){
					_font.setFont(new Font("Sans",Font.BOLD,20));
					_cmdList[0] = _fontList.get(5);

				}
				else if(_isItalic){
					_font.setFont(new Font("Sans",Font.ITALIC,20));
					_cmdList[0] = _fontList.get(6);

				}else {
					_font.setFont(new Font("Sans",Font.PLAIN,20));
					_cmdList[0] = _fontList.get(4);

				}
				
			
			}
			else{
				_font.setText("Serif");
				_isSerif = true;
				if(_isBold && _isItalic){
					_font.setFont(new Font("Serif",Font.BOLD | Font.ITALIC,20));
					_cmdList[0] = _fontList.get(11);

				}
				else if(_isBold){
					_font.setFont(new Font("Serif",Font.BOLD,20));
					_cmdList[0] = _fontList.get(9);
				}
				else if(_isItalic){
					_font.setFont(new Font("Serif",Font.ITALIC,20));
					_cmdList[0] = _fontList.get(10);

				}else {
					_font.setFont(new Font("Serif",Font.PLAIN,20));
					_cmdList[0] = _fontList.get(8);

				}
				
			}
		}
		else if(e.getSource() == _Bold){
			Font font = _font.getFont();
			if(_isBold && _isItalic){
				_isBold = false;
				_font.setFont(new Font(font.getName(),Font.ITALIC,20));
				if(font.getName().equals("Sans")){
					_cmdList[0] = _fontList.get(6);
				}
				else if(font.getName().equals("Mono")){
					_cmdList[0] = _fontList.get(2);

				}
				else if(font.getName().equals("Serif")){
					_cmdList[0] = _fontList.get(10);
				}	
			}
			else if(_isBold && !_isItalic){
				_isBold=false;
				_font.setFont(new Font(font.getName(),Font.PLAIN,20));
				if(font.getName().equals("Sans")){
					_cmdList[0] = _fontList.get(4);
				}
				else if(font.getName().equals("Mono")){
					_cmdList[0] = _fontList.get(0);

				}
				else if(font.getName().equals("Serif")){
					_cmdList[0] = _fontList.get(8);
				}	
			}
			else if(!_isBold && _isItalic){
				_isBold=true;
				_font.setFont(new Font(font.getName(),Font.BOLD | Font.ITALIC,20));
				if(font.getName().equals("Sans")){
					_cmdList[0] = _fontList.get(7);
				}
				else if(font.getName().equals("Mono")){
					_cmdList[0] = _fontList.get(3);

				}
				else if(font.getName().equals("Serif")){
					_cmdList[0] = _fontList.get(11);
				}	
			}
			else if(!_isBold && !_isItalic){
				_isBold=true;
				_font.setFont(new Font(font.getName(),Font.BOLD,20));
				if(font.getName().equals("Sans")){
					_cmdList[0] = _fontList.get(5);
				}
				else if(font.getName().equals("Mono")){
					_cmdList[0] = _fontList.get(1);

				}
				else if(font.getName().equals("Serif")){
					_cmdList[0] = _fontList.get(9);
				}	
			}
		}
		else if(e.getSource() == _Italic){
			Font font = _font.getFont();

			if(_isBold && _isItalic){
				_isItalic = false;
				_font.setFont(new Font(font.getName(),Font.BOLD,20));
				if(font.getName().equals("Sans")){
					_cmdList[0] = _fontList.get(5);
				}
				else if(font.getName().equals("Mono")){
					_cmdList[0] = _fontList.get(1);

				}
				else if(font.getName().equals("Serif")){
					_cmdList[0] = _fontList.get(9);
				}	
			}
			else if(_isBold && !_isItalic){
				_isItalic=true;
				_font.setFont(new Font(font.getName(),Font.BOLD|Font.ITALIC,20));
				if(font.getName().equals("Sans")){
					_cmdList[0] = _fontList.get(7);
				}
				else if(font.getName().equals("Mono")){
					_cmdList[0] = _fontList.get(3);

				}
				else if(font.getName().equals("Serif")){
					_cmdList[0] = _fontList.get(11);
				}	
			}
			else if(!_isBold && _isItalic){
				_isItalic=false;
				_font.setFont(new Font(font.getName(),Font.PLAIN,20));
				if(font.getName().equals("Sans")){
					_cmdList[0] = _fontList.get(4);
				}
				else if(font.getName().equals("Mono")){
					_cmdList[0] = _fontList.get(0);

				}
				else if(font.getName().equals("Serif")){
					_cmdList[0] = _fontList.get(8);
				}	
			}
			else if(!_isBold && !_isItalic){
				_isItalic=true;
				_font.setFont(new Font(font.getName(),Font.ITALIC,20));
				if(font.getName().equals("Sans")){
					_cmdList[0] = _fontList.get(6);
				}
				else if(font.getName().equals("Mono")){
					_cmdList[0] = _fontList.get(2);

				}
				else if(font.getName().equals("Serif")){
					_cmdList[0] = _fontList.get(10);
				}	
			}
		}
		//Allow user to change font size
		else if(e.getSource() == _fontSize){
			String size = JOptionPane.showInputDialog("Enter font size(number 1 to 70):");
			int fontSize = 16;
			if(size !=null){
				try{
					fontSize = Integer.parseInt(size);
					if(fontSize >= 1 && fontSize <= 70){
						_fontSize.setText("FontSize:"+fontSize);
						_cmdList[2]=size;
					}
					else{
						JOptionPane.showMessageDialog(null, "Please Enter numbers(1 to 70) only.");
					}
				}
				catch(NumberFormatException e1){
					JOptionPane.showMessageDialog(null, "Please Enter numbers(1 to 70) only.");
				}
			}
		}
		//Allow text to be added
		else if(e.getSource() == _textBtn){
			if(_cmdList[1] != null){
				String text = JOptionPane.showInputDialog(null, _cmdList[1], "Add Text", JOptionPane.INFORMATION_MESSAGE);
				_cmdList[1] = text;
			}
			else{
				String text = JOptionPane.showInputDialog(null, "Enter Text to add:", "Add Text", JOptionPane.INFORMATION_MESSAGE);
				_cmdList[1] = text;
			}
		}//preview options, check if inputs all valid, allow time to be chosen
		else if(e.getSource() == _preview){
			
			if(_cmdList[1] == null){
				JOptionPane.showMessageDialog(null, "Please Enter text to add!", "Error!", JOptionPane.INFORMATION_MESSAGE);
			}
			else if(_cmdList[4] == null || _cmdList[5] == null){
				JOptionPane.showMessageDialog(null, "Please Enter StartTimeTo or ToEndTime!", "Error!", JOptionPane.INFORMATION_MESSAGE);

			}
			else{
				if(_video.isPlaying()){
					_video.pause();
					_playPause.setText(">");
				}
				int startTime = 0;
				int length =(int)_video.getLength()/1000;
				int min = length/60;
				int sec = length%60;
				JOptionPane.showMessageDialog(null, "Enter the time to start previewing.\nPlease enter in order MINUTE -> SECOND.","Readme",JOptionPane.INFORMATION_MESSAGE);
				String userMin = JOptionPane.showInputDialog(null,"Enter the MINUTE to start previewing.\nLength of video is:"+min+" minutes "+sec+" seconds");
				if(userMin != null){
					try{
						int minGiven = Integer.parseInt(userMin);
						if(minGiven >= 0 && minGiven <= min){
							startTime = minGiven*60;
							String userSec = JOptionPane.showInputDialog(null,"Enter the SECOND to start previewing.\nLength of video is:"+min+" minutes "+sec+" seconds");
							if(userSec != null){
								try{
									int SecGiven = Integer.parseInt(userSec);
									if(!userMin.equals(min)){
										if(SecGiven >= 0 && SecGiven < 60){
											startTime = startTime + SecGiven;
											
											if(_cmdList[4] == null || _cmdList[4].equals("-1")){
												String cmd = "avplay -i "+_filePath + " -vf \"drawtext=fontfile='" + _cmdList[0]+"': text='"+_cmdList[1]+"': x='(main_w-text_w)/2': y='(main_h-text_h)/2': "+"fontsize="+_cmdList[2]+": fontcolor='"+_cmdList[3]+"': draw='gt(t,"+_cmdList[5]+")'";
												_cmd =  "drawtext=fontfile='" + _cmdList[0]+"': text='"+_cmdList[1]+"': x='(main_w-text_w)/2': y='(main_h-text_h)/2': "+"fontsize="+_cmdList[2]+": fontcolor='"+_cmdList[3]+"': draw='gt(t,"+_cmdList[5]+")'";
												for(String s:_cmdHist){
													if(s !=null){
														cmd = cmd + ":," + s;
													}
												}
												cmd = cmd +"\" -ss " + startTime;
												_isSave = false;
												cmd = this.execCmd(cmd);
											}
											else if(_cmdList[5] == null || _cmdList[5].equals("-1"))
											{
												String cmd = "avplay -i "+_filePath + " -vf \"drawtext=fontfile='" + _cmdList[0]+"': text='"+_cmdList[1]+"': x='(main_w-text_w)/2': y='(main_h-text_h)/2': "+"fontsize="+_cmdList[2]+": fontcolor='"+_cmdList[3]+"': draw='lt(t,"+_cmdList[4]+")'";
												_cmd =  "drawtext=fontfile='" + _cmdList[0]+"': text='"+_cmdList[1]+"': x='(main_w-text_w)/2': y='(main_h-text_h)/2': "+"fontsize="+_cmdList[2]+": fontcolor='"+_cmdList[3]+"': draw='lt(t,"+_cmdList[4]+")'";
												for(String s:_cmdHist){
													if(s !=null){
														cmd = cmd + ":," + s;
													}
												}
												cmd = cmd +"\" -ss " + startTime;
												_isSave = false;
												cmd = this.execCmd(cmd);
											}


										}
										else{
											JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND less than 60", "Error!", JOptionPane.INFORMATION_MESSAGE);

										}
									}
									else if(userMin.equals(min)){
										if(SecGiven >= 0 && SecGiven < sec){
											if(_cmdList[4] == null || _cmdList[4].equals("-1")){
												String cmd = "avplay -i "+_filePath + " -vf \"drawtext=fontfile='" + _cmdList[0]+"': text='"+_cmdList[1]+"': x='(main_w-text_w)/2': y='(main_h-text_h)/2': "+"fontsize="+_cmdList[2]+": fontcolor='"+_cmdList[3]+"': draw='gt(t,"+_cmdList[5]+")'";
												_cmd =  ":,drawtext=fontfile='" + _cmdList[0]+"': text='"+_cmdList[1]+"': x='(main_w-text_w)/2': y='(main_h-text_h)/2': "+"fontsize="+_cmdList[2]+": fontcolor='"+_cmdList[3]+"': draw='gt(t,"+_cmdList[5]+")'";
												for(String s:_cmdHist){
													if(s !=null){
														cmd = cmd + s;
													}
												}
												cmd = cmd +"\"";
												_isSave = false;
												cmd = this.execCmd(cmd);
											}
											else if(_cmdList[5] == null || _cmdList[5].equals("-1"))
											{
												String cmd = "avplay -i "+_filePath + " -vf \"drawtext=fontfile='" + _cmdList[0]+"': text='"+_cmdList[1]+"': x='(main_w-text_w)/2': y='(main_h-text_h)/2': "+"fontsize="+_cmdList[2]+": fontcolor='"+_cmdList[3]+"': draw='lt(t,"+_cmdList[4]+")'";
												_cmd =  ":,drawtext=fontfile='" + _cmdList[0]+"': text='"+_cmdList[1]+"': x='(main_w-text_w)/2': y='(main_h-text_h)/2': "+"fontsize="+_cmdList[2]+": fontcolor='"+_cmdList[3]+"': draw='lt(t,"+_cmdList[4]+")'";
												for(String s:_cmdHist){
													if(s !=null){
														cmd = cmd + s;
													}
												}
												cmd = cmd +"\"";
												_isSave = false;
												cmd = this.execCmd(cmd);
											}
										}
										else{
											JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND less than "+sec, "Error!", JOptionPane.INFORMATION_MESSAGE);

										}
									}
								}
								catch(NumberFormatException e1){
									JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND!","Error!", JOptionPane.INFORMATION_MESSAGE);
								}
							}
						}
						else{
							JOptionPane.showMessageDialog(null, "Please Enter a number for MINUTE less than "+min, "Error!", JOptionPane.INFORMATION_MESSAGE);

						}
					}
					catch(NumberFormatException e1){
						JOptionPane.showMessageDialog(null, "Please Enter a number for MINUTE less than "+min, "Error!", JOptionPane.INFORMATION_MESSAGE);
					}
					
				}
				
				
			}
			
			
		}
		//Get Starting time
		else if(e.getSource() == _startTimeBtn){
			int startTime = 0;
			int length =(int)_video.getLength()/1000;
			int min = length/60;
			int sec = length%60;
			JOptionPane.showMessageDialog(null, "The text will appear from start of the video to the time given.\nPlease enter in order MINUTE -> SECOND.\nNOTE:This will set ToEndTime to 0!","Readme",JOptionPane.INFORMATION_MESSAGE);
			String userMin = JOptionPane.showInputDialog(null,"Enter the MINUTE to end text.\nLength of video is:"+min+" minutes "+sec+" seconds");
			if(userMin != null){
				try{
					int minGiven = Integer.parseInt(userMin);
					if(minGiven >= 0 && minGiven <= min){
						startTime = minGiven*60;
						String userSec = JOptionPane.showInputDialog(null,"Enter the SECOND to end text.\nLength of video is:"+min+" minutes "+sec+" seconds");
						if(userSec != null){
							try{
								int SecGiven = Integer.parseInt(userSec);
								if(!userMin.equals(min)){
									if(SecGiven >= 0 && SecGiven < 60){
										startTime = startTime + SecGiven;
										_cmdList[4] = String.valueOf(startTime);
										_cmdList[5] = "-1";

									}
									else{
										JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND less than 60", "Error!", JOptionPane.INFORMATION_MESSAGE);

									}
								}
								else if(userMin.equals(min)){
									if(SecGiven >= 0 && SecGiven < sec){
										startTime = startTime + SecGiven;
										_cmdList[4] = String.valueOf(startTime);
										_cmdList[5] = "-1";
									}
									else{
										JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND less than "+sec, "Error!", JOptionPane.INFORMATION_MESSAGE);

									}
								}
							}
							catch(NumberFormatException e1){
								JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND!","Error!", JOptionPane.INFORMATION_MESSAGE);
							}
						}
					}
					else{
						JOptionPane.showMessageDialog(null, "Please Enter a number for MINUTE less than "+min, "Error!", JOptionPane.INFORMATION_MESSAGE);

					}
				}
				catch(NumberFormatException e1){
					JOptionPane.showMessageDialog(null, "Please Enter a number for MINUTE less than "+min, "Error!", JOptionPane.INFORMATION_MESSAGE);
				}
				
			}
		
		}
		//Get Ending time
		else if(e.getSource() == _endTimeBtn){
			int startTime = 0;
			int length =(int)_video.getLength()/1000;
			int min = length/60;
			int sec = length%60;
			JOptionPane.showMessageDialog(null, "The text will appear from time given to end of the video.\nPlease enter in order MINUTE -> SECOND.\nNOTE:This will set StartTimeTo to 0!","Readme",JOptionPane.INFORMATION_MESSAGE);
			String userMin = JOptionPane.showInputDialog(null,"Enter the MINUTE to end text, length of video is:"+min+" minutes "+sec+" seconds");
			if(userMin != null){
				try{
					int minGiven = Integer.parseInt(userMin);
					if(minGiven >= 0 && minGiven <= min){
						startTime = minGiven*60;
						String userSec = JOptionPane.showInputDialog(null,"Enter the SECOND to end text, length of video is:"+min+" minutes "+sec+" seconds");
						if(userSec != null){
							try{
								int SecGiven = Integer.parseInt(userSec);
								if(!userMin.equals(min)){
									if(SecGiven >= 0 && SecGiven < 60){
										startTime = startTime + SecGiven;
										_cmdList[5] = String.valueOf(startTime);
										_cmdList[4] = "-1";


									}
									else{
										JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND less than 60", "Error!", JOptionPane.INFORMATION_MESSAGE);

									}
								}
								else if(userMin.equals(min)){
									if(SecGiven >= 0 && SecGiven < sec){
										startTime = startTime + SecGiven;
										_cmdList[5] = String.valueOf(startTime);
										_cmdList[4] = "-1";
										
									}
									else{
										JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND less than "+sec, "Error!", JOptionPane.INFORMATION_MESSAGE);

									}
								}
							}
							catch(NumberFormatException e1){
								JOptionPane.showMessageDialog(null, "Please Enter a number for SECOND!","Error!", JOptionPane.INFORMATION_MESSAGE);
							}
						}
					}
					else{
						JOptionPane.showMessageDialog(null, "Please Enter a number for MINUTE less than "+min, "Error!", JOptionPane.INFORMATION_MESSAGE);

					}
				}
				catch(NumberFormatException e1){
					JOptionPane.showMessageDialog(null, "Please Enter a number for MINUTE less than "+min, "Error!", JOptionPane.INFORMATION_MESSAGE);
				}
				
			}
			
		}
		else if(e.getSource() == _save){
			if(_cmd != null && !_isSave){
				if(_cmdHist.size() > 0){
					if(!_cmd.equals(_cmdHist.get((_cmdHist.size()-1)))){
						_cmdHist.add(_cmd);
						JOptionPane.showMessageDialog(null, "Saved Change!");
						_isSave = true;
					}
					else{
						JOptionPane.showMessageDialog(null, "No Changes Yet");
					}
				}
				else{
					_cmdHist.add(_cmd);
					JOptionPane.showMessageDialog(null, "Saved Change!");
					_isSave = true;
				}
				
			}else if(_isSave){
				JOptionPane.showMessageDialog(null, "No Changes Yet");
			}
			else{
				JOptionPane.showMessageDialog(null, "Please Preview changes first!");
			}
			
		}
		else if(e.getSource() == _undoBtn){
			int answer = JOptionPane.showConfirmDialog(null, "This will undo the last SAVED change!!","WARNING!",JOptionPane.OK_CANCEL_OPTION);
			if(answer == 0){
				if(_cmdHist.size() > 0){
					_cmdHist.remove(_cmdHist.size()-1);
					_cmdList[1] = null;
					_cmdList[4] = null;
					_cmdList[5] = null;
					JOptionPane.showMessageDialog(null, "Undo Successful!");
				}
				else{
					JOptionPane.showMessageDialog(null, "No Changes Yet");
				}
			}
		}
		else if(e.getSource() == _export){
			if(_cmdHist.size() == 0){
				JOptionPane.showMessageDialog(null, "No Changes Yet");

			}
			else{
				String output = JOptionPane.showInputDialog(null,"Enter Output Video file Name(No File Extension needed):");
				
				String cmd =  "test -f " + output+".mp4" + " && echo \"found\" || echo \"not found\"";
				cmd = this.execCmd(cmd);
				if(cmd.equals("found")){
					int userChoice = JOptionPane.showOptionDialog(null, "File Name Exists!,Overwrite?", "Warning!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Overwrite","Cancel"}, "default");
					//0 is overwrite 1 is cancel
					if(userChoice == 0){
						//remove file
						cmd = "rm "+output+".mp4";
						this.execCmd(cmd);
						
						cmd = "avconv -i "+_filePath + " -vf \"" + _cmdHist.get(0);
						for(int i = 1; i <_cmdHist.size();i++){
							String s = _cmdHist.get(i);
							if(s != null){
								cmd = cmd + ":," + s;
							}
						}
						cmd = cmd + "\" -c:a copy "+output+".mp4";
						_doneLabel.setVisible(false);
						_waitWin.setVisible(true);
						_waitLabel.setVisible(true);

						_extractTask.setCmd(cmd);
						_extractTask.execute(); 
					   	_file = new File("VAMIXHistory"+File.separator+output+"History.txt");

						if(!_file.exists()){
							new File("VAMIXHistory").mkdir();
							
							try {
								_file.createNewFile();

							} catch (IOException e1) {
								e1.printStackTrace();
							}	
						}
						
						try {
							this.writeHist();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				else{
					cmd = "avconv -i "+_filePath + " -vf \"" + _cmdHist.get(0);
					for(int i = 1; i <_cmdHist.size();i++){
						String s = _cmdHist.get(i);
						if(s != null){
							cmd = cmd + ":," + s;
						}
					}
					cmd = cmd + "\" -c:a copy "+output+".mp4";
					_doneLabel.setVisible(false);
					_waitWin.setVisible(true);
					_waitLabel.setVisible(true);

					_extractTask.setCmd(cmd);
					_extractTask.execute();
					
				   	_file = new File("VAMIXHistory"+File.separator+output+"History.txt");

					if(!_file.exists()){
						new File("VAMIXHistory").mkdir();
						try {
							_file.createNewFile();

						} catch (IOException e1) {
							e1.printStackTrace();
						}	
					}
					
					try {
						this.writeHist();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
    private void writeHist() throws IOException  {
    	_file.delete();
    	BufferedWriter writer = new BufferedWriter(new FileWriter(_file,true));
    	
    	HashSet hs = new HashSet();
    	hs.addAll(_cmdHist);
    	_cmdHist.clear();
    	_cmdHist.addAll(hs);
    	
    	writer.write("");
    	
    	for(String s:_cmdHist){
			writer.append(s);
			writer.newLine();
		} 
		writer.close();
    }
}
