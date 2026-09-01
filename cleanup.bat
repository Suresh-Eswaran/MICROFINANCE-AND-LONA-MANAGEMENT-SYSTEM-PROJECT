@echo off
echo Deleting unused models...
del /Q src\main\java\com\example\microfinance_loan_system\model\AuditLog.java
del /Q src\main\java\com\example\microfinance_loan_system\model\ComplianceCalendar.java
del /Q src\main\java\com\example\microfinance_loan_system\model\CreditAssessmentHistory.java
del /Q src\main\java\com\example\microfinance_loan_system\model\CreditCommitteeDeliberation.java
del /Q src\main\java\com\example\microfinance_loan_system\model\FieldVisit.java
del /Q src\main\java\com\example\microfinance_loan_system\model\GroupMeeting.java
del /Q src\main\java\com\example\microfinance_loan_system\model\GroupSavingsRecord.java
del /Q src\main\java\com\example\microfinance_loan_system\model\MicrofinanceDirectory.java
del /Q src\main\java\com\example\microfinance_loan_system\model\NotificationLog.java
del /Q src\main\java\com\example\microfinance_loan_system\model\PermissionMatrix.java
del /Q src\main\java\com\example\microfinance_loan_system\model\PhotoCollateral.java
del /Q src\main\java\com\example\microfinance_loan_system\model\SecurityAuditLog.java
del /Q src\main\java\com\example\microfinance_loan_system\model\SystemConfig.java
del /Q src\main\java\com\example\microfinance_loan_system\model\UserDevice.java
del /Q src\main\java\com\example\microfinance_loan_system\model\WaiverRequest.java

echo Deleting unused DTOs...
del /Q src\main\java\com\example\microfinance_loan_system\dto\AuditLogRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\BroadcastRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\CibilRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\CommitteeSignOffRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\FieldNoteRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\GroupCollectionRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\GroupMeetingRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\GroupSavingsDepositRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\GrtPassRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\IncomeVerificationRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\LegalActionRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\LoanRejectionRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\MemberExitRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\OfflineSyncRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\OtsProposalRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\RestructuringRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\RoleSwitchRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\ScoreOverrideRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\SignAgreementRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\WaiverApplicationRequest.java
del /Q src\main\java\com\example\microfinance_loan_system\dto\WriteOffRecommendationResponse.java

echo Deleting unused Repositories...
del /Q src\main\java\com\example\microfinance_loan_system\repository\AuditLogRepository.java
del /Q src\main\java\com\example\microfinance_loan_system\repository\MicrofinanceDirectoryRepository.java

echo Cleanup complete!
pause
