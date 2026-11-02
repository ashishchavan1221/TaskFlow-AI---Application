import SwiftUI

/// Team Directory & Workgroups Screen matching TeamScreens.kt.
public struct TeamDirectoryView: View {
    @ObservedObject var viewModel: MainAppViewModel

    @State private var selectedTabSection: Int = 0 // 0: Personnel Roster, 1: Workgroups
    @State private var showAddMemberSheet: Bool = false

    public var body: some View {
        VStack(spacing: 0) {
            // Segmented Section Selector
            Picker("Team View", selection: $selectedTabSection) {
                Text("Personnel Roster (\(viewModel.members.count))").tag(0)
                Text("Workgroups (\(viewModel.groups.count))").tag(1)
            }
            .pickerStyle(SegmentedPickerStyle())
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color.darkSurface)

            ScrollView {
                VStack(spacing: 16) {
                    if selectedTabSection == 0 {
                        // PERSONNEL ROSTER
                        HStack {
                            Text("Organization Directory")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            Spacer()

                            if viewModel.activeRole == "admin" {
                                Button(action: { showAddMemberSheet = true }) {
                                    HStack(spacing: 4) {
                                        Image(systemName: "person.badge.plus")
                                        Text("Add Member")
                                    }
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 6)
                                    .background(Color.actionBlue)
                                    .cornerRadius(8)
                                }
                            }
                        }

                        ForEach(viewModel.members) { mem in
                            memberCard(mem: mem)
                        }
                    } else {
                        // WORKGROUPS SECTION
                        HStack {
                            Text("Operational Workgroups")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            Spacer()

                            Button(action: { viewModel.showAddGroupDialog = true }) {
                                HStack(spacing: 4) {
                                    Image(systemName: "plus")
                                    Text("New Workgroup")
                                }
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(Color.brandPurple)
                                .cornerRadius(8)
                            }
                        }

                        if viewModel.groups.isEmpty {
                            VStack(spacing: 8) {
                                Image(systemName: "person.3.fill")
                                    .font(.system(size: 36))
                                    .foregroundColor(Color.textSecondary.opacity(0.4))
                                Text("No workgroups formed")
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(Color.textPrimary)
                                Text("Form a workgroup with 1–2 managers and staff associates to enable dedicated real-time chat channels.")
                                    .font(.system(size: 12))
                                    .foregroundColor(Color.textSecondary)
                                    .multilineTextAlignment(.center)
                            }
                            .padding(30)
                        } else {
                            ForEach(viewModel.groups) { grp in
                                workgroupCard(grp: grp)
                            }
                        }
                    }
                }
                .padding(16)
            }
        }
        .background(Color.darkBackground.edgesIgnoringSafeArea(.all))
        .sheet(isPresented: $showAddMemberSheet) {
            AddMemberModal(viewModel: viewModel, onDismiss: { showAddMemberSheet = false })
        }
    }

    private func memberCard(mem: Member) -> some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(mem.isAdmin ? Color.accentCyan.opacity(0.2) : (mem.isManager ? Color.actionBlue.opacity(0.2) : Color.brandPurple.opacity(0.2)))
                    .frame(width: 44, height: 44)

                Text(mem.displayName.prefix(2).uppercased())
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(mem.isAdmin ? Color.accentCyan : (mem.isManager ? Color.actionBlue : Color.brandPurple))
            }

            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    Text(mem.displayName)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(Color.textPrimary)

                    Text(mem.role.uppercased())
                        .font(.system(size: 9, weight: .black))
                        .foregroundColor(Color.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(mem.isAdmin ? Color.accentCyan : (mem.isManager ? Color.actionBlue : Color.brandPurple))
                        .cornerRadius(4)
                }

                Text("\(mem.employeeId) • \(mem.departmentName)")
                    .font(.system(size: 11))
                    .foregroundColor(Color.textSecondary)

                if !mem.email.isEmpty {
                    Text(mem.email)
                        .font(.system(size: 10))
                        .foregroundColor(Color.textMuted)
                }
            }

            Spacer()
        }
        .padding(12)
        .enterpriseCard(isDark: viewModel.isDarkTheme)
    }

    private func workgroupCard(grp: TeamGroup) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                VStack(alignment: .leading, spacing: 3) {
                    Text(grp.name)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(Color.textPrimary)

                    Text(grp.description)
                        .font(.system(size: 12))
                        .foregroundColor(Color.textSecondary)
                }

                Spacer()

                Button(action: {
                    viewModel.selectGroup(grp)
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "bubble.left.fill")
                        Text("Chat")
                    }
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(Color.actionBlue)
                    .cornerRadius(8)
                }
            }

            Divider().background(Color.darkBorder)

            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Lead Managers (1–2):")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(Color.textSecondary)
                    Text(grp.managerNames.joined(separator: ", "))
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(Color.accentCyan)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    Text("Staff Team:")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(Color.textSecondary)
                    Text("\(grp.staffNames.count) Associates")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(Color.textPrimary)
                }
            }
        }
        .padding(14)
        .enterpriseCard(isDark: viewModel.isDarkTheme)
    }
}

/// Modal for adding a new employee/manager to the organization.
struct AddMemberModal: View {
    @ObservedObject var viewModel: MainAppViewModel
    var onDismiss: () -> Void

    @State private var name: String = ""
    @State private var email: String = ""
    @State private var role: String = "staff"
    @State private var departmentName: String = "Operations"
    @State private var phone: String = ""
    @State private var employeeId: String = "STF-\(Int.random(in: 1000...9999))"
    @State private var securityKey: String = "\(Int.random(in: 1000...9999))"

    var body: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)
                ScrollView {
                    VStack(alignment: .leading, spacing: 14) {
                        Text("Provision Organization Member")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color.textPrimary)

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Full Name *")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.textSecondary)
                            TextField("e.g. Ananya Rao", text: $name)
                                .padding(10)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(8)
                                .foregroundColor(Color.textPrimary)
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Corporate Email")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.textSecondary)
                            TextField("ananya@taskflow.com", text: $email)
                                .padding(10)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(8)
                                .foregroundColor(Color.textPrimary)
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Role Hierarchy")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.textSecondary)
                            Picker("Role", selection: $role) {
                                Text("Department Manager").tag("manager")
                                Text("Staff Associate").tag("staff")
                            }
                            .pickerStyle(SegmentedPickerStyle())
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Department")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.textSecondary)
                            TextField("e.g. Operations & Logistics", text: $departmentName)
                                .padding(10)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(8)
                                .foregroundColor(Color.textPrimary)
                        }

                        HStack(spacing: 12) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Employee ID")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(Color.textSecondary)
                                TextField("ID", text: $employeeId)
                                    .padding(10)
                                    .background(Color.darkSurfaceElevated)
                                    .cornerRadius(8)
                                    .foregroundColor(Color.accentCyan)
                            }
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Security PIN")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(Color.textSecondary)
                                TextField("PIN", text: $securityKey)
                                    .padding(10)
                                    .background(Color.darkSurfaceElevated)
                                    .cornerRadius(8)
                                    .foregroundColor(Color.actionBlue)
                            }
                        }

                        Button(action: {
                            let deptId = departmentName.lowercased().contains("eng") ? "eng" : "ops"
                            viewModel.createMember(
                                name: name,
                                employeeId: employeeId,
                                email: email,
                                role: role,
                                deptId: deptId,
                                deptName: departmentName,
                                phone: phone,
                                securityKey: securityKey
                            )
                            onDismiss()
                        }) {
                            Text("Add Member to Roster")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(name.isEmpty ? Color.gray : Color.actionBlue)
                                .cornerRadius(12)
                        }
                        .disabled(name.isEmpty)
                        .padding(.top, 10)
                    }
                    .padding(16)
                }
            }
            .navigationTitle("New Personnel")
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
