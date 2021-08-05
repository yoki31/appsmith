// This is different from the onboarding sagas.
import { Organization } from "constants/orgConstants";
import { ReduxActionTypes } from "constants/ReduxActionConstants";
import { all, put, select, takeLatest } from "redux-saga/effects";
import { getOrgs, getUserCurrentOrgId } from "selectors/organizationSelectors";
import { getNextEntityName } from "utils/AppsmithUtils";

// Creates a new application for new users when they signup
function* createApplicationSaga() {
  const currentOrgId: string | undefined = yield select(getUserCurrentOrgId);
  let currentOrganization: Organization | undefined;

  const userOrgs: Organization[] = yield select(getOrgs);

  // Find the org where we want to create an application
  // If the user has a currentOrg we use that else we use the first org in the list
  if (currentOrgId) {
    currentOrganization = userOrgs.find(
      (org) => org.organization.id === currentOrgId,
    );

    if (!currentOrganization) {
      currentOrganization = userOrgs[0];
    }
  } else {
    currentOrganization = userOrgs[0];
  }

  if (currentOrganization) {
    const applicationName = getNextEntityName(
      "Untitled application ",
      currentOrganization.applications.map((el: any) => el.name),
    );

    yield put({
      type: ReduxActionTypes.CREATE_APPLICATION_INIT,
      payload: {
        applicationName,
        orgId: currentOrganization.organization.id,
      },
    });
  }
}

export default function* onboardingSagas() {
  yield all([
    takeLatest(
      ReduxActionTypes.SG_ONBOARDING_CREATE_APPLICATION,
      createApplicationSaga,
    ),
  ]);
}
