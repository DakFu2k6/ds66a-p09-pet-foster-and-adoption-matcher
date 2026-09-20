Assignment Overview
This document defines nine projects for nine student groups. Each project has a comparable
minimum scope but a different domain problem and OOP design challenge.
Important boundary: the minimum requirements are the grading contract. Optional exten
sions are not required for full completion and cannot compensate for missing core behavior.
1 Shared Requirements
Every group must satisfy the following baseline requirements.
1.1 Technical Requirements
• Use Java SE and Java Swing. Use the JDK version announced for the class and PostgreSQL
as the required database.
• Separate domain classes, application/service logic, persistence logic, and Swing UI. UI event
handlers must not contain the complete business logic.
• Use encapsulation: domain state is private, constructors/factory methods preserve valid state,
and public methods express meaningful operations.
• Use at least one meaningful composition or aggregation relationship and one justified abstrac
tion through an interface or abstract class.
• Demonstrate polymorphism in a real workflow. A class diagram alone is not sufficient.
• Use a typed collection such as ArrayList<T>, HashMap<K,V>, or HashSet<T> where appropri
ate.
• Define and handle at least one domain-specific exception. Invalid input must produce an
actionable GUI message and must not terminate the application.
• Persist all core data in PostgreSQL through JDBC. Use a normalized schema, parameterized
PreparedStatement queries, and transactions where a workflow changes multiple tables.
• Use one design pattern because it solves a documented design problem. The pattern must
appear in code, UML, and a live demonstration.
1.2 Minimum Deliverables
• Source code with a clear package structure and a reproducible run instruction.
• A README containing requirements, setup, usage, sample data, known limitations, and
group member contributions.
• AUMLclass diagram showing important relationships and the selected design pattern.
• Ashort requirements document mapping each minimum requirement to an implemented fea
ture.
• Test evidence for normal cases, boundary cases, invalid input, and persistence reload.
4
• Alive demonstration following the acceptance scenarios in the assigned project brief.
• APostgreSQL schema script, seed data script, and documented database configuration.
• JDBC code using parameterized queries and transaction handling for multi-step operations.
1.3 Common Project Scope
Each project should normally contain 4–6 meaningful domain classes, 4–6 main workflows, 2–3
non-trivial business rules, and 3–5 GUI views. Authentication, online services, and payment
gateways are outside the minimum scope unless explicitly approved. PostgreSQL deployment
and local database setup are part of the minimum persistence requirement.

11 Project G9: Pet Foster and Adoption Matcher
11.1 Problem Context
An animal rescue center coordinates pets, foster families, and adoption applications. A responsi
ble placement must consider the pet’s care needs, the family’s housing conditions and experience,
compatibility, available capacity, and the current commitment status.
11.2 Final Product
The group will deliver a pet foster and adoption matching application. Staff can manage pet
profiles, family profiles, and applications, then evaluate a possible placement using several cri
teria. The application explains which criteria pass or fail and prevents a second placement after
a pet or family is committed. Main views should include Pets, Foster Families, Applications,
Matching Result, and Placement Dashboard.
11.3 Learning Focus and Core Model
Focus on specification-based matching and polymorphic pet profiles. Suggested classes are Pet,
CareProfile, FosterFamily, Application, and MatchCriterion.
11.4 Minimum Functional Requirements
1. Register pets with species, age, health notes, temperament, and care requirements.
2. Register foster families with household constraints, experience, and available capacity.
3. Create and update applications and evaluate compatibility using at least three criteria.
4. Use a common criterion abstraction so criteria can be combined with AND/OR behavior.
5. Prevent placement when a pet or family is already committed or capacity is exceeded.
6. Persist and reload pets, families, applications, criteria, and placement status in PostgreSQL
through JDBC.
11.5 OOP and Acceptance Scenarios
• Design: pet subtypes must expose genuinely different care behavior through polymorphism;
matching criteria must be composable objects.
• Demo 1: create a pet and family, then display why an application matches or fails.
• Demo 2: combine criteria and show how the result changes when one criterion is changed.
• Demo 3: attempt a second placement after commitment and show the rejected operation.
Optional extensions: foster history, appointment booking, or anonymized adoption reports.
15
12 Public Grading Rubric
The following rubric applies equally to all nine groups. The weights total 100 points.
Criterion
Points Public grading evidence
Git
Documentation
Code quality
Functionality
OOP design
GUI
Persistence
Exception handling
Design pattern
Demo presentation
5
5
15
15
20
10
10
5
5
10
Meaningful commit history, traceable member
contributions, and repository organization.
README, setup/run instructions, UML,
requirements mapping, test evidence, and known
limitations.
Naming, cohesion, package structure, low
duplication, readability, and maintainability.
Required workflows, business rules, boundary cases,
and acceptance scenarios work correctly.
Encapsulation, composition, justified
inheritance/interfaces, and observable polymorphism.
Complete Swing workflows, coherent layout, usable
navigation, and actionable validation feedback.
Reliable save/load, correct data mapping,
consistency after reload, and safe resource handling.
Meaningful domain exceptions, appropriate recovery,
and no swallowed or unexplained errors.
A justified pattern solves a real problem and is
visible in code, UML, and the demo.
Repeatable live demonstration, clear explanation,
requirement coverage, and individual understanding.
Total
100
The same rubric is used for every group.
12.1 Presentation and Contribution Rules
• Every member must present a meaningful part and answer questions about the submitted
code.
• The Git score is based on evidence of contribution, not on the number of commits alone.
• Afeature that is listed but cannot be demonstrated or inspected is not counted as complete.
• Optional extensions may strengthen the evidence within a criterion, but cannot compensate
for missing minimum requirements