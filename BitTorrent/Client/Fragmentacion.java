
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Fragmentacion {


static final String key= "Mary has one cat";

public ArrayList<String> fragmentar ( String SourceFileName ,String DESTINATION_PATH ,int CHUNK_SIZE)
 {

    File willBeRead = new File ( SourceFileName );
    int FILE_SIZE = (int) willBeRead.length();
    ArrayList<String> nameList = new ArrayList<> ();

    int NUMBER_OF_CHUNKS = 0;
    byte[] temporary = null;

    try {
        InputStream inStream = null;
        int totalBytesRead = 0;

        try {
                inStream = new BufferedInputStream ( new FileInputStream( willBeRead ));

                while ( totalBytesRead < FILE_SIZE )
                {
                        String PART_NAME =SourceFileName+"."+NUMBER_OF_CHUNKS+".bin";
                        int bytesRemaining = FILE_SIZE-totalBytesRead;
                        if ( bytesRemaining < CHUNK_SIZE ) // Remaining Data Part is Smaller Than CHUNK_SIZE
                                   // CHUNK_SIZE is assigned to remain volume
                        {
                                CHUNK_SIZE = bytesRemaining;
                        }
                        temporary = new byte[CHUNK_SIZE]; //Temporary Byte Array
                        int bytesRead = inStream.read(temporary, 0, CHUNK_SIZE);

                        if ( bytesRead > 0) // If bytes read is not empty
                        {
                                totalBytesRead += bytesRead;
                                NUMBER_OF_CHUNKS++;
                        }

                        write(CryptoUtils.encrypt(key, temporary),PART_NAME);
                        nameList.add(PART_NAME);
                }

        }catch (CryptoException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
	}finally {
             inStream.close();
        }
    }
    catch (FileNotFoundException ex)
    {
             ex.printStackTrace();
    }
    catch (IOException ex)
    {
             ex.printStackTrace();
    }
    return nameList;
 }
 
public byte[] read(File readfile){
     DataInputStream dis = null;
     byte[] bytes = null;
     bytes = new byte[(int)readfile.length()];
     int read = 0;
     int numRead = 0;
    try {
        
        dis = new DataInputStream(new FileInputStream(readfile));
        while (read < bytes.length && (numRead = dis.read(bytes, read, bytes.length - read)) >= 0) {
            read = read + numRead;
        }
        
    } catch (FileNotFoundException ex) {
        System.out.println("Error [FileNotFoundException] funcion read");
        return null;
    } catch (IOException ex) {
        System.out.println("Error [IOException] funcion read");
        return null;
    } finally {
        try {
            dis.close();
        } catch (IOException ex) {
            System.out.println("Error closing DataInputStream [IOException] funcion read");
        }
    }
    if (read < bytes.length) {
            System.out.println("Unable to read: " + readfile.getName());
            return null;
    }
    
    return bytes;
 }

 public void write(byte[] DataByteArray, String DestinationFileName){
            try {
                    OutputStream output = null;
                    try {
                        output = new BufferedOutputStream(new FileOutputStream(DestinationFileName));
                        output.write( DataByteArray );
                    }finally{
                        output.close();
                    }
                    
            }
            catch(FileNotFoundException ex){
                    System.out.println("Error [FileNotFoundException] funcion write:2");
            }
            catch(IOException ex){
                    System.out.println("Error [IOException] funcion write");
            }catch(java.lang.NullPointerException e){
                
            }
 }
 

 
public void write(byte[] DataByteArray, String DestinationFileName, int piece){
            try {   
                    OutputStream output = null;
                    try {
                        DestinationFileName+="."+piece+".bin";
                        output = new BufferedOutputStream(new FileOutputStream(DestinationFileName));
                        output.write( DataByteArray );

                    }finally{
                        output.close();
                    }
                    
            }
            catch(FileNotFoundException ex){
                    System.out.println("Error [FileNotFoundException] funcion write:3");
            }
            catch(IOException ex){
                    System.out.println("Error [IOException] funcion write:3");
            }
 }
 


 public boolean unirPartes (String SourceFileName, int pieces )
 {
        boolean res=true;
        ArrayList<String> nameList = new ArrayList<> ();

        for(int i=0;i<pieces;i++){
            String PART_NAME =SourceFileName+"."+i+".bin";
            nameList.add(PART_NAME);
        }       
        File[] file = new File[nameList.size()];
        byte AllFilesContent[] = null;

        int TOTAL_SIZE = 0;
        int FILE_NUMBER = nameList.size();
        int FILE_LENGTH = 0;
        int CURRENT_LENGTH=0;

        for ( int i=0; i<FILE_NUMBER; i++)
        {
                file[i] = new File (nameList.get(i));
                TOTAL_SIZE+=file[i].length();
        }
        try {
                AllFilesContent= new byte[TOTAL_SIZE]; // Length of All Files, Total Size
                InputStream inStream = null;
                
                for ( int j=0; j<FILE_NUMBER; j++)
                {
                        
                        inStream = new DataInputStream(new FileInputStream(file[j]));
                        
                        FILE_LENGTH = (int) file[j].length();
                        byte [] temp = new byte[FILE_LENGTH];
                        inStream.read(temp, 0, FILE_LENGTH);
                        temp = CryptoUtils.decrypt(key, temp);
                        FILE_LENGTH-=16;
                        System.arraycopy(temp, 0,AllFilesContent, CURRENT_LENGTH,FILE_LENGTH);
                        CURRENT_LENGTH+=FILE_LENGTH;
                        inStream.close();
                }

        }
        catch (FileNotFoundException e)
        {
                System.out.println("File not found " + e);
                res = false;
        }
        catch (IOException ioe)
        {
                System.out.println("Exception while reading the file " + ioe);
                res = false;
        }catch (CryptoException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("ArrayIndexOutOfBoundsException " );
        }
        finally 
        {
            write (AllFilesContent,SourceFileName);
            
        }

        
        return res;
  
 }
 }
