import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { SessionPackageComponent } from './list/session-package.component';
import { SessionPackageDetailComponent } from './detail/session-package-detail.component';
import { SessionPackageUpdateComponent } from './update/session-package-update.component';
import SessionPackageResolve from './route/session-package-routing-resolve.service';

const sessionPackageRoute: Routes = [
  {
    path: '',
    component: SessionPackageComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: SessionPackageDetailComponent,
    resolve: {
      sessionPackage: SessionPackageResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: SessionPackageUpdateComponent,
    resolve: {
      sessionPackage: SessionPackageResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: SessionPackageUpdateComponent,
    resolve: {
      sessionPackage: SessionPackageResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default sessionPackageRoute;
