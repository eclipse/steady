# Manual Assessment

The manual assessment can be done from the bugs frontend https://@@HOST@@/bugs, by setting the assessment to the appropriate value in the "Assessment (Manual)" column and clicking the "Save" button.

<center class='expandable'>
    [![start_page](../img/manual_assessment.jpg)](../img/manual_assessment.jpg)
</center>

Our recommendation is to always rely on code (manually inspecting it in the worst case) in order to take a decision. The versions indicated in the vulnerability's description were proved wrong in multiple cases.

The column "Patch eval" shows information about the results (if any) of the patch lib analyzer. By clicking on the cell the results obtained by code comparison for each elements of the bug change list are shown. If available, it is recommended to use them in order to take a decision about the vulnerability of the corresponding library version.
