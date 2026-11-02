import SwiftUI
import WebKit

/// Document Preview Modal for PDFs, images, and work proofs.
public struct DocumentPreviewModal: View {
    public var task: TaskItem
    public var isWorkDoc: Bool
    public var onDismiss: () -> Void

    private var targetUrl: String {
        isWorkDoc ? task.workDocUrl : task.attachmentUrl
    }

    private var targetName: String {
        isWorkDoc ? (task.workDocName.isEmpty ? "Work Proof Document" : task.workDocName) : (task.attachmentName.isEmpty ? "Task Attachment" : task.attachmentName)
    }

    public var body: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)

                if targetUrl.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "doc.text.fill")
                            .font(.system(size: 40))
                            .foregroundColor(Color.textSecondary.opacity(0.4))
                        Text("No Document Attached")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color.textPrimary)
                        Text("This deliverable does not contain an external attachment file.")
                            .font(.system(size: 12))
                            .foregroundColor(Color.textSecondary)
                    }
                } else {
                    VStack {
                        DocumentWebView(url: URL(string: targetUrl))
                    }
                }
            }
            .navigationTitle(targetName)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { onDismiss() }
                        .foregroundColor(Color.actionBlue)
                }
            }
        }
    }
}

struct DocumentWebView: UIViewRepresentable {
    let url: URL?

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.backgroundColor = .clear
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        if let url = url {
            let request = URLRequest(url: url)
            uiView.load(request)
        }
    }
}

/// Upload Work Document Modal (Worker Submission / Proof of Work).
public struct UploadWorkDocumentModal: View {
    public var task: TaskItem
    public var onUpload: (_ docName: String, _ docUrl: String, _ docType: String, _ remarks: String) -> Void
    public var onDismiss: () -> Void

    @State private var docName: String = ""
    @State private var docUrl: String = ""
    @State private var docType: String = "pdf"
    @State private var remarks: String = ""

    public var body: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)

                VStack(spacing: 16) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Task Deliverable")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color.textSecondary)
                        Text(task.title)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(Color.textPrimary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(12)
                    .background(Color.darkSurfaceElevated)
                    .cornerRadius(10)

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Document File Name")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color.textSecondary)
                        TextField("e.g. signed_invoice_verification.pdf", text: $docName)
                            .padding(10)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(8)
                            .foregroundColor(Color.textPrimary)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Document Cloud / Web URL")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color.textSecondary)
                        TextField("https://...", text: $docUrl)
                            .keyboardType(.URL)
                            .autocapitalization(.none)
                            .padding(10)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(8)
                            .foregroundColor(Color.textPrimary)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Verification Remarks / Notes")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color.textSecondary)
                        TextField("Notes explaining the submitted file...", text: $remarks)
                            .padding(10)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(8)
                            .foregroundColor(Color.textPrimary)
                    }

                    Spacer()

                    Button(action: {
                        let finalName = docName.isEmpty ? "proof_document.pdf" : docName
                        onUpload(finalName, docUrl, docType, remarks)
                        onDismiss()
                    }) {
                        Text("Submit Work Proof")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.actionBlue)
                            .cornerRadius(12)
                    }
                }
                .padding(16)
            }
            .navigationTitle("Upload Work Document")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { onDismiss() }
                        .foregroundColor(Color.textSecondary)
                }
            }
        }
    }
}
