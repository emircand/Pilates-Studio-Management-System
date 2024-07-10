import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { StaffComponent } from './list/staff.component';
import { StaffDetailComponent } from './detail/staff-detail.component';
import { StaffUpdateComponent } from './update/staff-update.component';
import StaffResolve from './route/staff-routing-resolve.service';

const staffRoute: Routes = [
  {
    path: '',
    component: StaffComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: StaffDetailComponent,
    resolve: {
      staff: StaffResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: StaffUpdateComponent,
    resolve: {
      staff: StaffResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: StaffUpdateComponent,
    resolve: {
      staff: StaffResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default staffRoute;
