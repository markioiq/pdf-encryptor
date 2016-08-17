package jp.co.insource;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

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
	    	File origFile = new File(args[i]);
			String origFilename = origFile.getAbsolutePath();
			
	    	File origDir = new File(FilenameUtils.getFullPath(origFilename));
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
				
				AccessPermission ap = new AccessPermission();

		    	// ローカルファイルを保護設定
				ap.setCanPrint(true);
				ap.setCanPrintDegraded(true);
				ap.setCanExtractForAccessibility(true);

				ap.setCanAssembleDocument(false);
				ap.setCanExtractContent(false);
				ap.setCanFillInForm(false);
				ap.setCanModify(false);
				ap.setCanModifyAnnotations(false);
				
				StandardProtectionPolicy spp =
						new StandardProtectionPolicy("z0625in", null, ap);
				spp.setEncryptionKeyLength(128);
				document.protect(spp);
				
				document.save(tempFilename);
				document.close();
				
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
