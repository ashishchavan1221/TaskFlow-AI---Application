import SwiftUI

/// Create Team Group Modal matching CreateTeamGroupModal in MainActivityKt.kt.
/// Enforces business rule: Exactly 1–2 Lead Managers (capped at 2) and multiple Staff Associates.
public struct CreateWorkgroupModal: View {
    @ObservedObject var viewModel: MainAppViewModel
    public var onDismiss: () -> Void

    @State private var groupName: String = ""
    @State private var groupDescription: String = ""
    @State private var selectedManagerIds: Set<String> = []
    @State private var selectedStaffIds: Set<String> = []
    @State private var validationWarning: String? = nil

    private var availableManagers: [Member] {
        viewModel.members.filter { $0.isManager || $0.isAdmin }
    }

    private var availableStaff: [Member] {
        viewModel.members.filter { $0.isStaff }
    }

    public var body: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)

                ScrollView {
                    VStack(alignment: .leading, spacing: 18) {
                        // Group Name
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Workgroup Name *")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.textPrimary)
                            TextField("e.g. Cross-Functional Cloud DevOps", text: $groupName)
                                .padding(10)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(8)
                                .foregroundColor(Color.textPrimary)
                        }

                        // Description
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Charter & Responsibilities")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.textPrimary)
                            TextField("Brief purpose of this team group...", text: $groupDescription)
                                .padding(10)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(8)
                                .foregroundColor(Color.textPrimary)
                        }

                        // Manager Selection (Max 2 rule callout)
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Text("Lead Managers (1–2 Required) *")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color.textPrimary)
                                Spacer()
                                Text("\(selectedManagerIds.count)/2 Selected")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(selectedManagerIds.count > 0 ? Color.accentCyan : Color.errorRed)
                            }

                            Text("Statutory Workgroup Rule: Must have 1 or 2 lead managers (maximum 2).")
                                .font(.system(size: 11))
                                .foregroundColor(Color.textMuted)

                            ForEach(availableManagers) { mem in
                                let isSelected = selectedManagerIds.contains(mem.id)
                                Button(action: { toggleManager(mem.id) }) {
                                    HStack {
                                        Image(systemName: isSelected ? "checkmark.square.fill" : "square")
                                            .foregroundColor(isSelected ? Color.actionBlue : Color.textSecondary)
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(mem.displayName)
                                                .font(.system(size: 13, weight: .bold))
                                                .foregroundColor(Color.textPrimary)
                                            Text("\(mem.employeeId) • \(mem.departmentName)")
                                                .font(.system(size: 11))
                                                .foregroundColor(Color.textSecondary)
                                        }
                                        Spacer()
                                    }
                                    .padding(10)
                                    .background(isSelected ? Color.actionBlue.opacity(0.15) : Color.darkSurfaceElevated)
                                    .cornerRadius(8)
                                }
                            }
                        }

                        // Staff Selection
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Text("Staff Associates")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color.textPrimary)
                                Spacer()
                                Text("\(selectedStaffIds.count) Assigned")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(Color.textSecondary)
                            }

                            ForEach(availableStaff) { mem in
                                let isSelected = selectedStaffIds.contains(mem.id)
                                Button(action: { toggleStaff(mem.id) }) {
                                    HStack {
                                        Image(systemName: isSelected ? "checkmark.square.fill" : "square")
                                            .foregroundColor(isSelected ? Color.brandPurple : Color.textSecondary)
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(mem.displayName)
                                                .font(.system(size: 13, weight: .bold))
                                                .foregroundColor(Color.textPrimary)
                                            Text("\(mem.employeeId) • \(mem.departmentName)")
                                                .font(.system(size: 11))
                                                .foregroundColor(Color.textSecondary)
                                        }
                                        Spacer()
                                    }
                                    .padding(10)
                                    .background(isSelected ? Color.brandPurple.opacity(0.15) : Color.darkSurfaceElevated)
                                    .cornerRadius(8)
                                }
                            }
                        }

                        if let warning = validationWarning {
                            Text(warning)
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.errorRed)
                        }

                        // Submit Button
                        Button(action: createGroup) {
                            Text("Form Workgroup")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(Color.actionBlue)
                                .cornerRadius(12)
                        }
                        .padding(.top, 10)
                    }
                    .padding(16)
                }
            }
            .navigationTitle("New Team Workgroup")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { onDismiss() }
                        .foregroundColor(Color.textSecondary)
                }
            }
        }
    }

    private func toggleManager(_ id: String) {
        if selectedManagerIds.contains(id) {
            selectedManagerIds.remove(id)
            validationWarning = nil
        } else {
            if selectedManagerIds.count >= 2 {
                validationWarning = "Maximum 2 lead managers allowed per workgroup."
                return
            }
            selectedManagerIds.insert(id)
            validationWarning = nil
        }
    }

    private func toggleStaff(_ id: String) {
        if selectedStaffIds.contains(id) {
            selectedStaffIds.remove(id)
        } else {
            selectedStaffIds.insert(id)
        }
    }

    private func createGroup() {
        guard !groupName.isEmpty else {
            validationWarning = "Please enter a workgroup name."
            return
        }
        guard selectedManagerIds.count >= 1 && selectedManagerIds.count <= 2 else {
            validationWarning = "Workgroup must have 1 or 2 lead managers (enforced maximum 2)."
            return
        }

        let mgrIds = Array(selectedManagerIds)
        let mgrNames = viewModel.members.filter { mgrIds.contains($0.id) }.map { $0.displayName }
        let stfIds = Array(selectedStaffIds)
        let stfNames = viewModel.members.filter { stfIds.contains($0.id) }.map { $0.displayName }

        viewModel.createTeamGroup(
            name: groupName,
            description: groupDescription,
            managerIds: mgrIds,
            managerNames: mgrNames,
            staffIds: stfIds,
            staffNames: stfNames
        )
        onDismiss()
    }
}
