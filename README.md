# TokenATM_backend_2023 Wiki

### 

## General Documents

[Feature *Specification*](https://www.notion.so/Feature-Specification-848e642a4042489d896e2cefd636d3af?pvs=21)

[Design and Rules](https://www.notion.so/Design-and-Rules-b3a1d527156440bbbdb83b26a157f948?pvs=21)

## Setup Documents

The only thing to setup TokenATM system for your course is setting up the configuration of tokenATM, and you can find every parameter in your Canvas course.

[Configuration setup](https://www.notion.so/Configuration-setup-03bf14269a30470c947b9267ecc3e02b?pvs=21)

### Technical Documents

[API Logic](https://www.notion.so/API-Logic-b835d69312d0404fb4544b5066ba273d?pvs=21)

## FAQ

**What is the risk to apply TokenATM for your course?**

The risk of applying TokenATM for your course is relatively low. The potential risk is related to the visibility of the resubmission assignments you create. There is a possibility that the system could disrupt the visibility of these assignments.

However, it's important to note that TokenATM does not have the ability to directly impact student scores or modify their assignments. The only Canvas API that TokenATM utilizes is the [resubmission assignment override](https://canvas.instructure.com/doc/api/assignments.html#method.assignment_overrides.update) API, which is responsible for adding or removing students. It does not perform any modifications to the actual assignment content or scores.

You can review all the APIs performed by TokenATM in the  [API Logic](https://www.notion.so/API-Logic-b835d69312d0404fb4544b5066ba273d?pvs=21) document, which provides further details on how the system operates.

Overall, as long as you closely monitor the visibility of resubmission assignments and address any issues promptly, the risk associated with using TokenATM for your course should be minimal.

**How long does it take to approve hundreds of student requests?**

The time required to approve hundreds of student requests depends on the number of API operations that need to be performed. Each Canvas API operation typically takes 1-5 seconds to process. The total time needed to process all requests at once can be calculated using the following formula:

Total processing time for all requests at once =
`(Number of different types of assignments)` x `(5 seconds)`

For example:

1. If there are 100 students who all submit resubmissions for Assignment A, it would take approximately 5 seconds to process all of those requests.
2. If there are 100 students, with 30 submitting resubmissions for Assignment A, 20 submitting resubmissions for Assignment B, and 50 submitting resubmissions for Assignment C, it would take approximately 15 seconds to process all of those requests

**What personal data stored on Token ATM?**

Student name, student id, student email, and their token amount.

**How secure is the Token ATM? Is it possible for a student to hack it?**

It is important to note that nothing can be guaranteed to be completely secure. However, efforts have been made to ensure the security of the Token ATM system. Firstly, the Canvas API operations are performed on the server side, not on the client-side (the student's computer). This means that students cannot see the actual API operations being performed, which prevents them from understanding the system's logic. Additionally, the Canvas token belonging to the TA is not exposed to the public.

Secondly, before the server executes a Canvas API operation, it verifies the user's authorization to ensure that it is an authorized TA and not a student. In other word, Even if students somehow manage to understand the system's logic, their actions will be blocked by the system as they lack the necessary authorization.

Thirdly, every action performed by the system is logged, allowing for the quick identification of any suspicious activity.

Please note that while these measures are in place to enhance security, it is still important to remain vigilant and continuously assess and address potential security risks.



Wiki update: 2023-07-21 by Ching-Yang Lin