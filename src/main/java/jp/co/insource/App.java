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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {
	private static Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws InterruptedException {
		if (args.length < 1) {
			return;
		}

		for (int i = 0; i < args.length; i++) {
			File origFile = new File(args[i]).getAbsoluteFile();
			String origFilename = origFile.getAbsolutePath();

			File origDir = new File(FilenameUtils.getFullPath(origFilename));
			log.info("保護設定します: {}", FilenameUtils.getName(origFilename));

			File tempDir = FileUtils.getTempDirectory();
			String tempFilename = (tempDir.getAbsolutePath() + "/" + FilenameUtils.getName(origFilename)).replace("\\",
					"/");
			File tempFile = new File(tempFilename);

			try {
				// ファイルをローカルにコピーする
				log.debug("    作業用ファイルにコピーします。");
				FileUtils.copyFileToDirectory(origFile, tempDir);

				// ファイルのプロパティ削除
				log.debug("    ファイルのプロパティを削除します。");
				PDDocument document = PDDocument.load(tempFilename);
				PDDocumentInformation info = document.getDocumentInformation();
				info.setAuthor("");
				info.setCreator("");
				info.setKeywords("");
				info.setProducer("");
				info.setSubject("");
				info.setTitle("");

				// ファイルの見開き設定を変更(見開き、表紙を別にする)
				log.debug("    ページの見開き設定を変更します。(表紙あり)");
				PDDocumentCatalog catalog = document.getDocumentCatalog();
				catalog.setPageLayout(PDDocumentCatalog.PAGE_LAYOUT_TWO_PAGE_RIGHT);

				// 保護設定
				AccessPermission currentPermission = document.getCurrentAccessPermission();
				if (!currentPermission.canAssembleDocument() && 
						! currentPermission.canExtractContent() &&
						! currentPermission.canFillInForm() &&
						! currentPermission.canModify() &&
						! currentPermission.canModifyAnnotations()) {
					
					log.debug("    保護設定済みです。(印刷可・アクセシビリティのための内容の抽出可)");
				} else {

					// ローカルファイルを保護設定
					log.debug("    保護設定します。(印刷可・アクセシビリティのための内容の抽出可)");
					AccessPermission ap = new AccessPermission();
					ap.setCanPrint(true);
					ap.setCanPrintDegraded(true);
					ap.setCanExtractForAccessibility(true);

					ap.setCanAssembleDocument(false);
					ap.setCanExtractContent(false);
					ap.setCanFillInForm(false);
					ap.setCanModify(false);
					ap.setCanModifyAnnotations(false);

					StandardProtectionPolicy spp = new StandardProtectionPolicy("z0625in", null, ap);
					spp.setEncryptionKeyLength(128);
					document.protect(spp);

					document.save(tempFilename);
					document.close();
				}

				// ローカルファイルをサーバにアップする
				log.debug("    作業用ファイルで上書きします。");
				FileUtils.copyFileToDirectory(tempFile, origDir);

				log.info("    保護設定できました。");

			} catch (Exception e) {
				log.error("保護設定ができませんでした: ", FilenameUtils.getName(origFilename));
				log.error("{}", e.getLocalizedMessage());
				log.debug("例外発生: ", e);

			}

		}

	}

}
