import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IStaff } from '../staff.model';
import { StaffService } from '../service/staff.service';

export const staffResolve = (route: ActivatedRouteSnapshot): Observable<null | IStaff> => {
  const id = route.params['id'];
  if (id) {
    return inject(StaffService)
      .find(id)
      .pipe(
        mergeMap((staff: HttpResponse<IStaff>) => {
          if (staff.body) {
            return of(staff.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default staffResolve;
