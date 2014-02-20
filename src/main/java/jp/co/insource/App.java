package jp.co.insource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Encrypt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	if (args.length < 1) {
    		return;
    	}
    	
    	for (int i = 0; i < args.length; i++) {
			String origFilename = args[i];
			
	    	File origDir = new File(FilenameUtils.getFullPath(origFilename));
	    	File origFile = new File(origFilename);
	    	System.out.println("保護設定します: " + FilenameUtils.getName(origFilename));
	    	
	    	File tempDir = FileUtils.getTempDirectory();
	    	String tempFilename = (tempDir.getAbsolutePath() + "/" + FilenameUtils.getName(origFilename)).replace("\\", "/");
	    	File tempFile = new File(tempFilename);
	    	
	    	try {
	        	// ファイルをローカルにコピーする
				FileUtils.copyFileToDirectory(origFile, tempDir);

				// ファイルのプロパティ削除
				PDDocument document = PDDocument.load(tempFilename);
				PDDocumentInformation info = document.getDocumentInformation();
				info.setAuthor("");
				info.setCreator("");
				info.setKeywords("");
				info.setProducer("");
				info.setSubject("");
				info.setTitle("");
				// ファイルの見開き設定を変更(見開き、表紙を別にする)
				PDDocumentCatalog catalog = document.getDocumentCatalog();
				catalog.setPageLayout(PDDocumentCatalog.PAGE_LAYOUT_TWO_PAGE_RIGHT);
				document.save(tempFilename);
				document.close();

		    	// ローカルファイルを保護設定
				List<String> argList = new ArrayList<String>();
				argList.add("-canPrint");
				argList.add("true");
				argList.add("-canPrintDegraded");
				argList.add("true");
				argList.add("-canAssemble");
				argList.add("false");
				argList.add("-canExtractContent");
				argList.add("false");
				argList.add("-canExtractForAccessibility");
				argList.add("false");
				argList.add("-canFillInForm");
				argList.add("false");
				argList.add("-canModify");
				argList.add("false");
				argList.add("-canModifyAnnotations");
				argList.add("false");
				argList.add("-keyLength");
				argList.add("128");
				argList.add("-O");
				argList.add("z0625in");
				argList.add(tempFilename);
				String[] arg = (String[]) argList.toArray(new String[argList.size()]);
				Encrypt.main(arg);
				
		    	// ローカルファイルをサーバにアップする
				FileUtils.copyFileToDirectory(tempFile, origDir);
			} catch (Exception e) {
				e.printStackTrace();
				String fullPath = FilenameUtils.getFullPath(origFilename);
				String fileName = "エラー：" + FilenameUtils.getBaseName(origFilename) + ".txt";
				try {
					FileUtils.touch(new File(fullPath + "/" + fileName));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		}
    	
    }
    
}
