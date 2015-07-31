/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
//import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;

//import com.sun.org.apache.bcel.internal.classfile.LineNumber;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.LineInputStream;


public class CheckListGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if(args.length == 0)
		{
			System.out.println("No input parameter. Use an input text file.");
			return;
		}

		LineInputStream lin;
		DataOutputStream dos;
		Error_Message[] messageArr = new Error_Message[100000];
		
		//create input/output streams
		try
		{
			File input = new File(args[0]);
  		    File output = new File(input.getAbsolutePath().substring(0, input.getAbsolutePath().indexOf(input.getName())) + "output.txt");
			FileInputStream bf = new FileInputStream(input);
			lin = new LineInputStream(bf);
			FileOutputStream bo = new FileOutputStream(output);
		    dos = new DataOutputStream(bo);
		    
		}
		catch (Exception e)
		{
			System.out.println("Fileopen unsuccessful ...");
			e.printStackTrace();
			return;
		}
		
		StringBuffer outline = new StringBuffer();
		Integer rowcounter = 0;
		
		int actErrLoc = 0;
		String actMessage = " ";
		int emptycounter = 0;
		String line;
		String filename;
//***************************		
//loading data 
//***************************		
    	try
		{   //EOF cannot be detected
			while(emptycounter < 50)
			{
				//System.out.println(line);
				line = lin.readLine();
				//empty line
				if( line == null || line.isEmpty())
				{
					//dos.writeChars("\n");
					messageArr[rowcounter] = new Error_Message(" ", -1, " ", true);
					rowcounter++;
					emptycounter++;
					continue;
				}
				emptycounter = 0;
				StringTokenizer errortokenizer = new StringTokenizer(line, "\t");
				
				actMessage = errortokenizer.nextElement().toString();
				errortokenizer.nextElement();
				filename = errortokenizer.nextElement().toString();
				
				StringTokenizer linetokenizer = new StringTokenizer(errortokenizer.nextElement().toString(), " ");
				if(!linetokenizer.nextElement().toString().equals("line"))
				{
					//System.out.println(rowcounter);
					//System.out.println(line);
					actErrLoc = -1;
				}
				else 
				{
					actErrLoc = new Integer(linetokenizer.nextElement().toString()).intValue();
				}
				
				
				
				
				messageArr[rowcounter] = new Error_Message(actMessage, actErrLoc, filename, false);
				rowcounter++;
			}	
	    }
		catch (Exception e)
		{
			System.out.println("Riched EOF. Rowcounter:" + rowcounter);
			e.printStackTrace();
		}	
		
		/*for(int i = 0; i < rowcounter; i++ )
		{
			System.out.println(messageArr[i].text + " Line: " + new Integer(messageArr[i].line).toString());
			
		}
		System.out.println(rowcounter);*/
		
//***************************
//writing out converted lines
//***************************
		
		int i, j;
		int counter;
		String prevFileName = " ";
		String actFileName = " ";
		boolean filechange = false;
		String markerArrayPostfix = "";
		int filecounter = 0;
		
		for(i = 0; i < rowcounter; i++)
		{
			outline = new StringBuffer("");
			//empty message means empty line
			if(messageArr[i].isempty)
			{
				outline.append("\n");
			}
			else
			{
				actFileName = messageArr[i].filename;
				//checking filename
				if(!actFileName.equals(prevFileName) && !actFileName.equals(" "))
				{
					filecounter+=1;
					outline.append("\n\n\n\n");
					outline.append("//" + actFileName + "\n");
					outline.append("lineNum = " + new Integer(messageArr[i].line).toString() + ";\n");
					if(filecounter > 1)markerArrayPostfix = new Integer(filecounter).toString();
				}
					
				j = i;
				counter = 1;
				//checking how many lines has the same message continously
				while((messageArr[j].text.equals(messageArr[j+1].text) && messageArr[j].line + 1 == messageArr[j+1].line))
				{
					counter++;
					j++;
				}
				//checking how many message are the same for a codeline
				if(counter == 1)
				while(messageArr[j].text.equals(messageArr[j+1].text) && messageArr[j].line == messageArr[j+1].line)
				{
					counter++;
					j++;
				}
				
				if(counter == 1)
				{
					if(!actFileName.equals(messageArr[i+1].filename)) filechange = true;
					outline.append("markersToCheck").append(markerArrayPostfix).append(".add(new MarkerToCheck(\"").append(messageArr[i].text).append("\", ");
					if(messageArr[i].line == messageArr[i+1].line)
					{
						outline.append("lineNum, IMarker.SEVERITY_ERROR ));\n");
					}
					else
					{
						outline.append("lineNum++, IMarker.SEVERITY_ERROR ));\n");
						if(!messageArr[i+1].isempty && (messageArr[i+1].line - messageArr[i].line !=1) && !filechange)
						{
							outline.append("lineNum += " + new Integer(messageArr[i+1].line - messageArr[i].line - 1).toString() + ";\n");
						}
					}
				}
				else
				{
					if(!actFileName.equals(messageArr[i+counter].filename)) filechange = true;
					outline.append("for(i = 0; i < " + new Integer(counter).toString() + "; i++)");
					outline.append("markersToCheck").append(markerArrayPostfix).append(".add(new MarkerToCheck(\"").append(messageArr[i].text).append("\", ");
					if((messageArr[i].line != messageArr[i+1].line))
					{
						outline.append("lineNum++, IMarker.SEVERITY_ERROR ));\n");
								
						i = i + counter - 1;
						
						if((messageArr[i].line == messageArr[i+1].line) && !filechange)
						{
							outline.append("lineNum--;\n");
						}else
						if(!messageArr[i+1].isempty && (messageArr[i+1].line - messageArr[i].line !=1) && !filechange)
						{
							outline.append("lineNum += " + new Integer(messageArr[i+1].line - messageArr[i].line - 1).toString() + ";\n");
						}
					
					}
					else
					{
						outline.append("lineNum, IMarker.SEVERITY_ERROR ));\n");
						
						i = i + counter - 1;
						
						if(!(messageArr[i+1].isempty) && (messageArr[i+1].line - messageArr[i].line != 0) && !filechange)
						{
							outline.append("lineNum += " + new Integer(messageArr[i+1].line - messageArr[i].line).toString() + ";\n");
						}
					}
				}
			}
			
			prevFileName = actFileName;
			filechange = false;
			//writing out the converted line
			try{
			System.out.println(outline.toString());
			dos.writeBytes(outline.toString());
			dos.flush();
			}
			catch (Exception e)
			{
			   e.printStackTrace();
			   return;
			}
			
		}
			
	}
	
}
