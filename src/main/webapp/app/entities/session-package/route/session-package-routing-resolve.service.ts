import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ISessionPackage } from '../session-package.model';
import { SessionPackageService } from '../service/session-package.service';

export const sessionPackageResolve = (route: ActivatedRouteSnapshot): Observable<null | ISessionPackage> => {
  const id = route.params['id'];
  if (id) {
    return inject(SessionPackageService)
      .find(id)
      .pipe(
        mergeMap((sessionPackage: HttpResponse<ISessionPackage>) => {
          if (sessionPackage.body) {
            return of(sessionPackage.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default sessionPackageResolve;
